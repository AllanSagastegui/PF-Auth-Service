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
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.port.in.VerifyMfaInputPort;
import pe.ask.auth.core.port.in.command.VerifyMfaCommand;
import pe.ask.auth.core.port.in.result.VerifyMfaResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Duration;

@UseCase
public final class VerifyMfaUseCase implements VerifyMfaInputPort {

    private static final Duration SESSION_TTL = Duration.ofDays(14);

    private final UserRepositoryOutputPort userRepository;
    private final SessionRepositoryOutputPort sessionRepository;
    private final RefreshTokenRepositoryOutputPort refreshTokenRepository;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final TotpOutputPort totpPort;
    private final ClockOutputPort clockPort;
    private final IdGeneratorOutputPort idGenerator;

    public VerifyMfaUseCase(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            TotpOutputPort totpPort,
            ClockOutputPort clockPort,
            IdGeneratorOutputPort idGenerator
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.refreshTokenRepository = DomainValidation.requireNonNull(refreshTokenRepository, "refreshTokenRepository");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.totpPort = DomainValidation.requireNonNull(totpPort, "totpPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
        this.idGenerator = DomainValidation.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public Mono<VerifyMfaResult> verifyMfa(VerifyMfaCommand command) {
        return tokenGenerator.verifyMfaChallengeToken(command.mfaChallengeToken())
                .switchIfEmpty(Mono.error(new InvalidTokenException()))
                .flatMap(userId -> userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                        .flatMap(user -> validateUserAndTotp(user, command.totpCode())
                                .then(Mono.defer(() -> createSessionAndIssueTokens(user, command)))));
    }

    private Mono<Void> validateUserAndTotp(User user, String totpCode) {
        if (user.status() == UserStatus.LOCKED) {
            return Mono.error(new AccountLockedException());
        }
        if (user.status() != UserStatus.ACTIVE) {
            return Mono.error(new AccountDisabledException());
        }
        if (!user.mfaEnabled() || user.totpSecretEncrypted() == null) {
            return Mono.error(new InvalidCredentialsException());
        }

        return totpPort.decryptSecret(user.totpSecretEncrypted())
                .flatMap(rawSecret -> totpPort.verifyCode(rawSecret, totpCode))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .then();
    }

    private Mono<VerifyMfaResult> createSessionAndIssueTokens(User user, VerifyMfaCommand command) {
        return clockPort.now()
                .flatMap(now -> idGenerator.nextId()
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
                                                                        .thenReturn(new VerifyMfaResult(
                                                                                tokens.accessToken(),
                                                                                tokens.rawRefreshToken(),
                                                                                tokens.expiresInSeconds(),
                                                                                tokens.tokenType()
                                                                        ));
                                                            })));
                                })));
    }
}
