package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.port.in.command.LogoutCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private RefreshTokenRepositoryOutputPort refreshTokenRepository;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private ClockOutputPort clockPort;

    private LogoutUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID familyId = UUID.randomUUID();
    private final UUID tokenId = UUID.randomUUID();
    private final UUID deviceId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                clockPort
        );
    }

    @Test
    @DisplayName("Should revoke refresh token and session on logout when device ID matches")
    void shouldLogoutSuccessfully() {
        LogoutCommand cmd = new LogoutCommand("raw_ref_token", deviceId);
        RefreshToken token = new RefreshToken(
                tokenId,
                sessionId,
                familyId,
                userId,
                deviceId,
                "hash_val",
                RefreshTokenStatus.ACTIVE,
                now.minusSeconds(100),
                now.plusSeconds(86400),
                null,
                1L
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("raw_ref_token")).thenReturn(Mono.just("hash_val"));
        when(refreshTokenRepository.findByTokenHash("hash_val")).thenReturn(Mono.just(token));
        when(refreshTokenRepository.update(any(RefreshToken.class))).thenReturn(Mono.empty());
        when(sessionRepository.revokeById(sessionId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.logout(cmd))
                .expectNextMatches(res -> res.message().contains("Logged out successfully"))
                .verifyComplete();

        verify(refreshTokenRepository).update(argThat(t -> t.status() == RefreshTokenStatus.REVOKED));
        verify(sessionRepository).revokeById(sessionId);
    }

    @Test
    @DisplayName("Should return generic success even if token not found")
    void shouldReturnGenericSuccessWhenTokenNotFound() {
        LogoutCommand cmd = new LogoutCommand("nonexistent_token", deviceId);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("nonexistent_token")).thenReturn(Mono.just("nonexistent_hash"));
        when(refreshTokenRepository.findByTokenHash("nonexistent_hash")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.logout(cmd))
                .expectNextMatches(res -> res.message().contains("Logged out successfully"))
                .verifyComplete();

        verify(refreshTokenRepository, never()).update(any());
        verify(sessionRepository, never()).revokeById(any());
    }
}
