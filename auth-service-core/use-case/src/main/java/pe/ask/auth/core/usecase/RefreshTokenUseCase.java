package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.constant.DomainConstants;
import pe.ask.auth.core.model.exception.AccountDisabledException;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.IdempotencyConflictException;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.model.exception.TokenExpiredException;
import pe.ask.auth.core.model.exception.UnauthorizedException;
import pe.ask.auth.core.model.exception.ValidationException;
import pe.ask.auth.core.port.in.RefreshTokenInputPort;
import pe.ask.auth.core.port.in.command.RefreshTokenCommand;
import pe.ask.auth.core.port.in.result.RefreshTokenResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.IdempotencyOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SecurityAuditOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@UseCase
public final class RefreshTokenUseCase implements RefreshTokenInputPort {

    private static final Duration REFRESH_TTL = Duration.ofDays(14);
    private static final Duration IDEMPOTENCY_WINDOW = Duration.ofSeconds(60);
    private static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final String MSG_IDEMPOTENCY_REQUIRED = "Header Idempotency-Key is required";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:refresh:";
    private static final String EVENT_REUSE_DETECTED = "REFRESH_TOKEN_REUSE_DETECTED";
    private static final String PAYLOAD_DELIMITER = "|||";
    private static final String PAYLOAD_DELIMITER_REGEX = "\\|\\|\\|";

    private final UserRepositoryOutputPort userRepository;
    private final SessionRepositoryOutputPort sessionRepository;
    private final RefreshTokenRepositoryOutputPort refreshTokenRepository;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final IdempotencyOutputPort idempotencyPort;
    private final SecurityAuditOutputPort auditPort;
    private final ClockOutputPort clockPort;
    private final IdGeneratorOutputPort idGenerator;

    public RefreshTokenUseCase(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            IdempotencyOutputPort idempotencyPort,
            SecurityAuditOutputPort auditPort,
            ClockOutputPort clockPort,
            IdGeneratorOutputPort idGenerator
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.refreshTokenRepository = DomainValidation.requireNonNull(refreshTokenRepository, "refreshTokenRepository");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.idempotencyPort = DomainValidation.requireNonNull(idempotencyPort, "idempotencyPort");
        this.auditPort = DomainValidation.requireNonNull(auditPort, "auditPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
        this.idGenerator = DomainValidation.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public Mono<RefreshTokenResult> refresh(RefreshTokenCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            return Mono.error(new ValidationException(Map.of(HEADER_IDEMPOTENCY_KEY, MSG_IDEMPOTENCY_REQUIRED)));
        }

        return clockPort.now()
                .flatMap(now -> tokenGenerator.hashToken(command.rawRefreshToken())
                        .flatMap(tokenHash -> {
                            String idempotencyCompositeKey = IDEMPOTENCY_PREFIX + command.idempotencyKey() + ":" + command.deviceId();
                            String payloadHash = tokenHash + ":" + (command.requestBodyHash() != null ? command.requestBodyHash() : "");

                            return idempotencyPort.acquireLock(idempotencyCompositeKey, payloadHash, IDEMPOTENCY_WINDOW)
                                    .flatMap(acquired -> !acquired
                                            ? idempotencyPort.getCachedResponse(idempotencyCompositeKey)
                                                    .flatMap(this::deserializeCachedResponse)
                                                    .switchIfEmpty(Mono.error(new IdempotencyConflictException()))
                                            : executeRefreshRotation(tokenHash, command, now, idempotencyCompositeKey));
                        }));
    }

    private Mono<RefreshTokenResult> executeRefreshRotation(
            String tokenHash,
            RefreshTokenCommand command,
            Instant now,
            String idempotencyCompositeKey
    ) {
        return refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .switchIfEmpty(Mono.error(new InvalidTokenException()))
                .flatMap(token -> {
                    if (token.status() == RefreshTokenStatus.ROTATED) {
                        return handleReuseAttack(token, now);
                    }

                    if (token.status() == RefreshTokenStatus.REVOKED) {
                        return Mono.error(new UnauthorizedException());
                    }

                    if (token.expiresAt().isBefore(now)) {
                        return Mono.error(new TokenExpiredException());
                    }

                    if (!token.deviceId().equals(command.deviceId())) {
                        return handleReuseAttack(token, now);
                    }

                    return sessionRepository.findById(token.sessionId())
                            .switchIfEmpty(Mono.error(new UnauthorizedException()))
                            .flatMap(session -> {
                                if (!session.isActive(now)) {
                                    return Mono.error(new UnauthorizedException());
                                }

                                return userRepository.findById(token.userId())
                                        .switchIfEmpty(Mono.error(new UnauthorizedException()))
                                        .flatMap(user -> {
                                            if (user.status() != UserStatus.ACTIVE) {
                                                return Mono.error(new AccountDisabledException());
                                            }

                                            RefreshToken rotatedOldToken = token.rotate(now);
                                            return refreshTokenRepository.update(rotatedOldToken)
                                                    .then(tokenGenerator.issueTokens(user, session, token.familyId()))
                                                    .flatMap(tokens -> tokenGenerator.hashToken(tokens.rawRefreshToken())
                                                            .flatMap(nextTokenHash -> idGenerator.nextId()
                                                                    .flatMap(nextTokenId -> {
                                                                        RefreshToken nextToken = new RefreshToken(
                                                                                nextTokenId,
                                                                                session.id(),
                                                                                token.familyId(),
                                                                                user.id(),
                                                                                command.deviceId(),
                                                                                nextTokenHash,
                                                                                RefreshTokenStatus.ACTIVE,
                                                                                now,
                                                                                now.plus(REFRESH_TTL),
                                                                                null,
                                                                                1L
                                                                        );

                                                                        Session updatedSession = session.touch(now, now.plus(REFRESH_TTL));

                                                                        return sessionRepository.update(updatedSession)
                                                                                .then(refreshTokenRepository.save(nextToken))
                                                                                .flatMap(saved -> {
                                                                                    RefreshTokenResult result = new RefreshTokenResult(
                                                                                            tokens.accessToken(),
                                                                                            tokens.rawRefreshToken(),
                                                                                            tokens.expiresInSeconds(),
                                                                                            tokens.tokenType()
                                                                                    );

                                                                                    String cachePayload = serializeResult(result);
                                                                                    return idempotencyPort.saveCachedResponse(
                                                                                            idempotencyCompositeKey,
                                                                                            cachePayload,
                                                                                            IDEMPOTENCY_WINDOW
                                                                                    ).thenReturn(result);
                                                                                });
                                                                    })));
                                        });
                            });
                });
    }

    private <T> Mono<T> handleReuseAttack(RefreshToken token, Instant now) {
        return refreshTokenRepository.revokeFamily(token.familyId(), now)
                .then(sessionRepository.revokeById(token.sessionId()))
                .then(auditPort.recordSecurityEvent(EVENT_REUSE_DETECTED, token.userId(), "FamilyId: " + token.familyId(), now))
                .then(Mono.error(new UnauthorizedException()));
    }

    private String serializeResult(RefreshTokenResult result) {
        return result.accessToken() + PAYLOAD_DELIMITER + result.refreshToken() + PAYLOAD_DELIMITER + result.expiresInSeconds() + PAYLOAD_DELIMITER + result.tokenType();
    }

    private Mono<RefreshTokenResult> deserializeCachedResponse(String raw) {
        if (raw == null || !raw.contains(PAYLOAD_DELIMITER)) {
            return Mono.empty();
        }
        String[] parts = raw.split(PAYLOAD_DELIMITER_REGEX);
        if (parts.length < 4) {
            return Mono.empty();
        }
        return Mono.just(new RefreshTokenResult(parts[0], parts[1], Long.parseLong(parts[2]), parts[3]));
    }
}
