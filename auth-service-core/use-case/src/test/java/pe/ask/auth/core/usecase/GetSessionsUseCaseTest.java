package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.port.in.command.GetSessionsCommand;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSessionsUseCaseTest {

    @Mock
    private SessionRepositoryOutputPort sessionRepository;

    private GetSessionsUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID deviceId1 = UUID.randomUUID();
    private final UUID deviceId2 = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetSessionsUseCase(sessionRepository);
    }

    @Test
    @DisplayName("Should return active sessions for user")
    void shouldReturnActiveSessions() {
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        Session session1 = new Session(s1, userId, deviceId1, SessionStatus.ACTIVE, "1.1.1.1", "Chrome", now, now, now.plusSeconds(86400));
        Session session2 = new Session(s2, userId, deviceId2, SessionStatus.ACTIVE, "2.2.2.2", "Firefox", now, now, now.plusSeconds(86400));

        when(sessionRepository.findActiveByUserId(userId)).thenReturn(Flux.just(session1, session2));

        StepVerifier.create(useCase.getSessions(new GetSessionsCommand(userId)))
                .expectNextMatches(res ->
                        res.sessions().size() == 2 &&
                        res.sessions().stream().anyMatch(s -> s.id().equals(s1)) &&
                        res.sessions().stream().anyMatch(s -> s.id().equals(s2))
                )
                .verifyComplete();
    }
}
