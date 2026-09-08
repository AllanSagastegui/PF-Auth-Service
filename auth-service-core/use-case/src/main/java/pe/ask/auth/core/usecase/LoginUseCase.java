package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.AccountDisabledException;
import pe.ask.auth.core.model.exception.AccountLockedException;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.EmailNotVerifiedException;
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.port.in.LoginInputPort;
import pe.ask.auth.core.port.in.command.LoginCommand;
import pe.ask.auth.core.port.in.result.LoginResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@UseCase
public final class LoginUseCase implements LoginInputPort {

    private static final Duration SESSION_TTL = Duration.ofDays(14);
    private static final long MAX_ACTIVE_SESSIONS = 10;

    private final UserRepositoryOutputPort userRepository;
    private final SessionRepositoryOutputPort sessionRepository;
    private final RefreshTokenRepositoryOutputPort refreshTokenRepository;
    private final PasswordHasherOutputPort passwordHasher;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final ClockOutputPort clockPort;
    private final IdGeneratorOutputPort idGenerator;

    public LoginUseCase(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clockPort,
            IdGeneratorOutputPort idGenerator
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.refreshTokenRepository = DomainValidation.requireNonNull(refreshTokenRepository, "refreshTokenRepository");
        this.passwordHasher = DomainValidation.requireNonNull(passwordHasher, "passwordHasher");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
        this.idGenerator = DomainValidation.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public Mono<LoginResult> login(LoginCommand command) {
        String canonicalEmail = User.canonicalizeEmail(command.email());

        return clockPort.now()
                .flatMap(now -> userRepository.findByCanonicalEmail(canonicalEmail)
                        .flatMap(user -> handleExistingUser(user, command, now))
                        .switchIfEmpty(Mono.defer(() -> handleNonExistingUser(command))));
    }

    private Mono<LoginResult> handleExistingUser(User user, LoginCommand command, Instant now) {
        return passwordHasher.verifyPassword(command.password(), user.passwordHash())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .then(validateUserStatus(user))
                .flatMap(validUser -> validUser.mfaEnabled()
                        ? tokenGenerator.createMfaChallengeToken(validUser.id(), command.deviceId())
                                .map(LoginResult::mfaRequired)
                        : createSessionAndIssueTokens(validUser, command, now));
    }

    private Mono<User> validateUserStatus(User user) {
        if (user.status() == UserStatus.LOCKED) {
            return Mono.error(new AccountLockedException());
        }
        if (user.status() == UserStatus.DISABLED || user.status() == UserStatus.DELETED) {
            return Mono.error(new AccountDisabledException());
        }
        if (user.status() == UserStatus.PENDING_VERIFICATION) {
            return Mono.error(new EmailNotVerifiedException());
        }
        if (user.status() != UserStatus.ACTIVE) {
            return Mono.error(new InvalidCredentialsException());
        }
        return Mono.just(user);
    }

    private Mono<LoginResult> handleNonExistingUser(LoginCommand command) {
        return passwordHasher.verifyDummyPassword(command.password())
                .then(Mono.error(new InvalidCredentialsException()));
    }

    private Mono<LoginResult> createSessionAndIssueTokens(User user, LoginCommand command, Instant now) {
        return sessionRepository.countActiveByUserId(user.id())
                .filter(count -> count >= MAX_ACTIVE_SESSIONS)
                .flatMap(count -> sessionRepository.findByUserIdAndDeviceId(user.id(), command.deviceId())
                        .flatMap(existingSession -> sessionRepository.revokeById(existingSession.id())))
                .then(idGenerator.nextId())
                .flatMap(sessionId -> idGenerator.nextId()
                        .flatMap(familyId -> {
                            Session session = new Session(
                                    sessionId,
                                    user.id(),
                                    command.deviceId(),
                                    SessionStatus.ACTIVE,
                                    command.ipAddress(),
                                    command.userAgent(),
                                    now,
                                    now,
                                    now.plus(SESSION_TTL)
                            );

                            return sessionRepository.save(session)
                                    .then(tokenGenerator.issueTokens(user, session, familyId))
                                    .flatMap(tokens -> tokenGenerator.hashToken(tokens.rawRefreshToken())
                                            .flatMap(tokenHash -> idGenerator.nextId()
                                                    .flatMap(refreshTokenId -> {
                                                        RefreshToken refreshToken = new RefreshToken(
                                                                refreshTokenId,
                                                                sessionId,
                                                                familyId,
                                                                user.id(),
                                                                command.deviceId(),
                                                                tokenHash,
                                                                RefreshTokenStatus.ACTIVE,
                                                                now,
                                                                now.plus(SESSION_TTL),
                                                                null,
                                                                1L
                                                        );

                                                        return refreshTokenRepository.save(refreshToken)
                                                                .thenReturn(LoginResult.success(
                                                                        tokens.accessToken(),
                                                                        tokens.rawRefreshToken(),
                                                                        tokens.expiresInSeconds()
                                                                ));
                                                    })));
                        }));
    }
}
