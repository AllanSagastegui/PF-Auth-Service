package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.port.in.EmailVerificationRequestInputPort;
import pe.ask.auth.core.port.in.command.EmailVerificationRequestCommand;
import pe.ask.auth.core.port.in.result.EmailVerificationRequestResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Duration;

@UseCase
public final class EmailVerificationRequestUseCase implements EmailVerificationRequestInputPort {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final String GENERIC_RESPONSE = "If the account exists and is pending verification, a new link has been sent.";

    private final UserRepositoryOutputPort userRepository;
    private final OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final NotificationOutputPort notificationPort;
    private final ClockOutputPort clockPort;
    private final IdGeneratorOutputPort idGenerator;

    public EmailVerificationRequestUseCase(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            NotificationOutputPort notificationPort,
            ClockOutputPort clockPort,
            IdGeneratorOutputPort idGenerator
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.oneTimeTokenRepository = DomainValidation.requireNonNull(oneTimeTokenRepository, "oneTimeTokenRepository");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.notificationPort = DomainValidation.requireNonNull(notificationPort, "notificationPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
        this.idGenerator = DomainValidation.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public Mono<EmailVerificationRequestResult> requestVerification(EmailVerificationRequestCommand command) {
        String canonicalEmail = User.canonicalizeEmail(command.email());

        return clockPort.now()
                .flatMap(now -> userRepository.findByCanonicalEmail(canonicalEmail)
                        .flatMap(user -> {
                            if (user.status() != UserStatus.PENDING_VERIFICATION) {
                                return Mono.empty();
                            }
                            return oneTimeTokenRepository.revokeByUserIdAndType(user.id(), OneTimeTokenType.EMAIL_VERIFICATION, now)
                                    .then(tokenGenerator.generateSecureToken())
                                    .flatMap(rawToken -> tokenGenerator.hashToken(rawToken)
                                            .flatMap(tokenHash -> idGenerator.nextId()
                                                    .flatMap(tokenId -> {
                                                        OneTimeToken token = new OneTimeToken(
                                                                tokenId,
                                                                user.id(),
                                                                tokenHash,
                                                                OneTimeTokenType.EMAIL_VERIFICATION,
                                                                now,
                                                                now.plus(TOKEN_TTL),
                                                                false,
                                                                null
                                                        );
                                                        return oneTimeTokenRepository.save(token)
                                                                .then(notificationPort.sendVerificationEmail(user.rawEmail(), rawToken));
                                                    })));
                        }))
                .then(Mono.just(new EmailVerificationRequestResult(GENERIC_RESPONSE)));
    }
}
