package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.AuthTokens;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.IdempotencyConflictException;
import pe.ask.auth.core.model.exception.UnauthorizedException;
import pe.ask.auth.core.model.exception.ValidationException;
import pe.ask.auth.core.port.in.command.RefreshTokenCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.IdempotencyOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SecurityAuditOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepositoryOutputPort refreshTokenRepository;
    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private IdempotencyOutputPort idempotencyPort;
    @Mock
    private SecurityAuditOutputPort auditPort;
    @Mock
    private ClockOutputPort clockPort;
    @Mock
    private IdGeneratorOutputPort idGenerator;

    private RefreshTokenUseCase useCase;

    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID familyId = UUID.randomUUID();
    private final UUID oldTokenId = UUID.randomUUID();
    private final UUID newTokenId = UUID.randomUUID();
    private final UUID deviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                idempotencyPort,
                auditPort,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when Idempotency-Key header is missing")
    void shouldRejectMissingIdempotencyKey() {
        RefreshTokenCommand cmd = new RefreshTokenCommand("raw_token", deviceId, null, "bodyhash", "127.0.0.1", "Agent");

        StepVerifier.create(useCase.refresh(cmd))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should replay cached response when idempotency key is locked and cached")
    void shouldReplayCachedResponse() {
        RefreshTokenCommand cmd = new RefreshTokenCommand("raw_token", deviceId, "idem-1", "bodyhash", "127.0.0.1", "Agent");
        String cachedSerialized = "cached_acc|||cached_ref|||900|||Bearer";

        String lockKey = "idempotency:refresh:idem-1:" + deviceId;
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("raw_token")).thenReturn(Mono.just("raw_hash"));
        when(idempotencyPort.acquireLock(eq(lockKey), any(), any())).thenReturn(Mono.just(false));
        when(idempotencyPort.getCachedResponse(eq(lockKey))).thenReturn(Mono.just(cachedSerialized));

        StepVerifier.create(useCase.refresh(cmd))
                .expectNextMatches(res ->
                        "cached_acc".equals(res.accessToken()) &&
                        "cached_ref".equals(res.refreshToken()) &&
                        res.expiresInSeconds() == 900L
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw IdempotencyConflictException when lock is acquired by another concurrent request and response not yet cached")
    void shouldThrowConflictWhenLockBusyWithoutCache() {
        RefreshTokenCommand cmd = new RefreshTokenCommand("raw_token", deviceId, "idem-1", "bodyhash", "127.0.0.1", "Agent");
        String lockKey = "idempotency:refresh:idem-1:" + deviceId;

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("raw_token")).thenReturn(Mono.just("raw_hash"));
        when(idempotencyPort.acquireLock(eq(lockKey), any(), any())).thenReturn(Mono.just(false));
        when(idempotencyPort.getCachedResponse(eq(lockKey))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.refresh(cmd))
                .expectError(IdempotencyConflictException.class)
                .verify();
    }

    @Test
    @DisplayName("Should detect token reuse when token is ROTATED, revoking family and session")
    void shouldDetectReuseAndRevokeFamily() {
        RefreshTokenCommand cmd = new RefreshTokenCommand("reused_token", deviceId, "idem-2", "bodyhash", "127.0.0.1", "Agent");
        String lockKey = "idempotency:refresh:idem-2:" + deviceId;

        RefreshToken rotatedToken = new RefreshToken(
                oldTokenId,
                sessionId,
                familyId,
                userId,
                deviceId,
                "reused_hash",
                RefreshTokenStatus.ROTATED,
                now.minusSeconds(100),
                now.plusSeconds(86400),
                now.minusSeconds(10),
                2L
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("reused_token")).thenReturn(Mono.just("reused_hash"));
        when(idempotencyPort.acquireLock(eq(lockKey), any(), any())).thenReturn(Mono.just(true));
        when(refreshTokenRepository.findByTokenHashForUpdate("reused_hash")).thenReturn(Mono.just(rotatedToken));
        when(refreshTokenRepository.revokeFamily(eq(familyId), eq(now))).thenReturn(Mono.empty());
        when(sessionRepository.revokeById(eq(sessionId))).thenReturn(Mono.empty());
        when(auditPort.recordSecurityEvent(eq("REFRESH_TOKEN_REUSE_DETECTED"), eq(userId), any(), eq(now)))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.refresh(cmd))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(refreshTokenRepository).revokeFamily(familyId, now);
        verify(sessionRepository).revokeById(sessionId);
        verify(auditPort).recordSecurityEvent(eq("REFRESH_TOKEN_REUSE_DETECTED"), eq(userId), any(), eq(now));
    }

    @Test
    @DisplayName("Should detect device mismatch and handle reuse attack")
    void shouldRejectDeviceMismatch() {
        UUID otherDevice = UUID.randomUUID();
        RefreshTokenCommand cmd = new RefreshTokenCommand("raw_token", deviceId, "idem-3", "bodyhash", "127.0.0.1", "Agent");
        String lockKey = "idempotency:refresh:idem-3:" + deviceId;

        RefreshToken token = new RefreshToken(
                oldTokenId,
                sessionId,
                familyId,
                userId,
                otherDevice,
                "token_hash",
                RefreshTokenStatus.ACTIVE,
                now.minusSeconds(100),
                now.plusSeconds(86400),
                null,
                1L
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("raw_token")).thenReturn(Mono.just("token_hash"));
        when(idempotencyPort.acquireLock(eq(lockKey), any(), any())).thenReturn(Mono.just(true));
        when(refreshTokenRepository.findByTokenHashForUpdate("token_hash")).thenReturn(Mono.just(token));
        when(refreshTokenRepository.revokeFamily(eq(familyId), eq(now))).thenReturn(Mono.empty());
        when(sessionRepository.revokeById(eq(sessionId))).thenReturn(Mono.empty());
        when(auditPort.recordSecurityEvent(eq("REFRESH_TOKEN_REUSE_DETECTED"), eq(userId), any(), eq(now)))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.refresh(cmd))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(refreshTokenRepository).revokeFamily(familyId, now);
    }

    @Test
    @DisplayName("Should perform regular rotation and cache idempotency result")
    void shouldRotateTokenSuccessfully() {
        RefreshTokenCommand cmd = new RefreshTokenCommand("valid_token", deviceId, "idem-4", "bodyhash", "127.0.0.1", "Agent");
        String lockKey = "idempotency:refresh:idem-4:" + deviceId;

        RefreshToken activeToken = new RefreshToken(
                oldTokenId,
                sessionId,
                familyId,
                userId,
                deviceId,
                "valid_hash",
                RefreshTokenStatus.ACTIVE,
                now.minusSeconds(100),
                now.plusSeconds(86400),
                null,
                1L
        );
        Session session = new Session(sessionId, userId, deviceId, SessionStatus.ACTIVE, "127.0.0.1", "Agent", now.minusSeconds(300), now.minusSeconds(300), now.plusSeconds(86400));
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("valid_token")).thenReturn(Mono.just("valid_hash"));
        when(idempotencyPort.acquireLock(eq(lockKey), any(), any())).thenReturn(Mono.just(true));
        when(refreshTokenRepository.findByTokenHashForUpdate("valid_hash")).thenReturn(Mono.just(activeToken));
        when(refreshTokenRepository.update(argThat(t -> t.status() == RefreshTokenStatus.ROTATED))).thenReturn(Mono.just(activeToken.rotate(now)));
        when(tokenGenerator.issueTokens(eq(user), any(Session.class), eq(familyId)))
                .thenReturn(Mono.just(new AuthTokens("new_access_token", "new_refresh_token", 900L, "Bearer")));
        when(tokenGenerator.hashToken("new_refresh_token")).thenReturn(Mono.just("new_refresh_hash"));
        when(sessionRepository.findById(sessionId)).thenReturn(Mono.just(session));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(idGenerator.nextId()).thenReturn(Mono.just(newTokenId));
        when(sessionRepository.update(any(Session.class))).thenReturn(Mono.just(session.touch(now, now.plusSeconds(86400))));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(idempotencyPort.saveCachedResponse(eq(lockKey), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.refresh(cmd))
                .expectNextMatches(res ->
                        "new_access_token".equals(res.accessToken()) &&
                        "new_refresh_token".equals(res.refreshToken())
                )
                .verifyComplete();

        verify(refreshTokenRepository).update(argThat(t -> t.status() == RefreshTokenStatus.ROTATED));
        verify(refreshTokenRepository).save(argThat(t -> t.tokenHash().equals("new_refresh_hash")));
        verify(idempotencyPort).saveCachedResponse(eq(lockKey), any(), any());
    }
}
