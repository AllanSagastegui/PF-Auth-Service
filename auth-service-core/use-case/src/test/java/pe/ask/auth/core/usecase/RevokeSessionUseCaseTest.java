package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.port.in.command.RevokeSessionCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeSessionUseCaseTest {

    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private RefreshTokenRepositoryOutputPort refreshTokenRepository;
    @Mock
    private ClockOutputPort clockPort;

    private RevokeSessionUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID deviceId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new RevokeSessionUseCase(
                sessionRepository,
                refreshTokenRepository,
                clockPort
        );
    }

    @Test
    @DisplayName("Should revoke session and refresh tokens when session belongs to requesting user")
    void shouldRevokeMatchingSession() {
        RevokeSessionCommand cmd = new RevokeSessionCommand(userId, sessionId);
        Session session = new Session(sessionId, userId, deviceId, SessionStatus.ACTIVE, "1.1.1.1", "Agent", now, now, now.plusSeconds(86400));

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(sessionRepository.findById(sessionId)).thenReturn(Mono.just(session));
        when(sessionRepository.revokeById(sessionId)).thenReturn(Mono.empty());
        when(refreshTokenRepository.revokeBySessionId(sessionId, now)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.revokeSession(cmd))
                .expectNextMatches(res -> res.message().contains("revoked successfully"))
                .verifyComplete();

        verify(sessionRepository).revokeById(sessionId);
        verify(refreshTokenRepository).revokeBySessionId(sessionId, now);
    }

    @Test
    @DisplayName("Should not revoke session if session belongs to another user")
    void shouldNotRevokeOtherUserSession() {
        UUID otherUserId = UUID.randomUUID();
        RevokeSessionCommand cmd = new RevokeSessionCommand(userId, sessionId);
        Session session = new Session(sessionId, otherUserId, deviceId, SessionStatus.ACTIVE, "1.1.1.1", "Agent", now, now, now.plusSeconds(86400));

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(sessionRepository.findById(sessionId)).thenReturn(Mono.just(session));

        StepVerifier.create(useCase.revokeSession(cmd))
                .expectNextMatches(res -> res.message().contains("revoked successfully"))
                .verifyComplete();

        verify(sessionRepository, never()).revokeById(sessionId);
        verify(refreshTokenRepository, never()).revokeBySessionId(any(), any());
    }
}
