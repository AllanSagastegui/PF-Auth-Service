package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.constant.DomainConstants;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.model.exception.UserAlreadyExistsException;
import pe.ask.auth.core.model.exception.ValidationException;
import pe.ask.auth.core.port.in.RegisterUserInputPort;
import pe.ask.auth.core.port.in.command.RegisterUserCommand;
import pe.ask.auth.core.port.in.result.RegisterUserResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.OutboxRepositoryOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@UseCase
public final class RegisterUserUseCase implements RegisterUserInputPort {

    private static final int MIN_PASSWORD_LENGTH = 15;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofMinutes(30);
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_EMAIL = "email";
    private static final String AT_SIGN = "@";
    private static final String USER_REGISTERED_JSON_TEMPLATE = "{\"userId\":\"%s\",\"email\":\"%s\",\"status\":\"%s\"}";

    private final UserRepositoryOutputPort userRepository;
    private final OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    private final OutboxRepositoryOutputPort outboxRepository;
    private final PasswordHasherOutputPort passwordHasher;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final NotificationOutputPort notificationPort;
    private final ClockOutputPort clockPort;
    private final IdGeneratorOutputPort idGenerator;

    public RegisterUserUseCase(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            OutboxRepositoryOutputPort outboxRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            NotificationOutputPort notificationPort,
            ClockOutputPort clockPort,
            IdGeneratorOutputPort idGenerator
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.oneTimeTokenRepository = DomainValidation.requireNonNull(oneTimeTokenRepository, "oneTimeTokenRepository");
        this.outboxRepository = DomainValidation.requireNonNull(outboxRepository, "outboxRepository");
        this.passwordHasher = DomainValidation.requireNonNull(passwordHasher, "passwordHasher");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.notificationPort = DomainValidation.requireNonNull(notificationPort, "notificationPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
        this.idGenerator = DomainValidation.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public Mono<RegisterUserResult> register(RegisterUserCommand command) {
        return validateCommand(command)
                .then(Mono.defer(clockPort::now))
                .flatMap(now -> {
                    String rawEmail = command.email().strip();
                    String canonicalEmail = User.canonicalizeEmail(rawEmail);

                    return userRepository.existsByCanonicalEmail(canonicalEmail)
                            .filter(exists -> !exists)
                            .switchIfEmpty(Mono.error(new UserAlreadyExistsException()))
                            .flatMap(ignored -> passwordHasher.hashPassword(command.password()))
                            .flatMap(hashedPassword -> createAndSaveUser(rawEmail, canonicalEmail, hashedPassword, now))
                            .flatMap(user -> createVerificationTokenAndOutbox(user, now)
                                    .thenReturn(new RegisterUserResult(
                                            user.id(),
                                            user.rawEmail(),
                                            DomainConstants.MSG_REGISTRATION_SUCCESS
                                    )));
                });
    }

    private Mono<Void> validateCommand(RegisterUserCommand command) {
        if (command.password() == null || command.password().length() < MIN_PASSWORD_LENGTH || command.password().length() > MAX_PASSWORD_LENGTH) {
            return Mono.error(new PasswordPolicyException(Map.of(
                    FIELD_PASSWORD, DomainConstants.MSG_INVALID_PASSWORD_LENGTH
            )));
        }
        if (command.email() == null || command.email().isBlank() || !command.email().contains(AT_SIGN)) {
            return Mono.error(new ValidationException(Map.of(
                    FIELD_EMAIL, DomainConstants.MSG_INVALID_EMAIL
            )));
        }
        return Mono.empty();
    }

    private Mono<User> createAndSaveUser(String rawEmail, String canonicalEmail, String hashedPassword, Instant now) {
        return idGenerator.nextId()
                .flatMap(userId -> {
                    User newUser = new User(
                            userId,
                            rawEmail,
                            canonicalEmail,
                            hashedPassword,
                            UserStatus.PENDING_VERIFICATION,
                            Set.of(Role.ROLE_USER),
                            1L,
                            false,
                            null,
                            now,
                            now
                    );
                    return userRepository.save(newUser);
                });
    }

    private Mono<Void> createVerificationTokenAndOutbox(User user, Instant now) {
        return tokenGenerator.generateSecureToken()
                .flatMap(rawToken -> tokenGenerator.hashToken(rawToken)
                        .flatMap(tokenHash -> idGenerator.nextId()
                                .flatMap(tokenId -> {
                                    OneTimeToken token = new OneTimeToken(
                                            tokenId,
                                            user.id(),
                                            tokenHash,
                                            OneTimeTokenType.EMAIL_VERIFICATION,
                                            now,
                                            now.plus(VERIFICATION_TOKEN_TTL),
                                            false,
                                            null
                                    );

                                    return oneTimeTokenRepository.save(token)
                                            .then(notificationPort.sendVerificationEmail(user.rawEmail(), rawToken))
                                            .then(createOutboxMessage(user, now));
                                })));
    }

    private Mono<Void> createOutboxMessage(User user, Instant now) {
        return idGenerator.nextId()
                .flatMap(outboxId -> {
                    String payload = String.format(USER_REGISTERED_JSON_TEMPLATE,
                            user.id(), user.rawEmail(), user.status());

                    OutboxMessage outboxMessage = new OutboxMessage(
                            outboxId,
                            DomainConstants.AGGREGATE_TYPE_USER,
                            user.id().toString(),
                            DomainConstants.EVENT_USER_REGISTERED_V1,
                            payload,
                            now,
                            now,
                            null,
                            0
                    );

                    return outboxRepository.save(outboxMessage).then();
                });
    }
}
