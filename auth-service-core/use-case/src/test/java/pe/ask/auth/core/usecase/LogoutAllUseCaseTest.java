package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.port.in.command.LogoutAllCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutAllUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private ClockOutputPort clockPort;

    private LogoutAllUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new LogoutAllUseCase(
                userRepository,
                sessionRepository,
                clockPort
        );
    }

    @Test
    @DisplayName("Should revoke all user sessions and increment auth version")
    void shouldRevokeAllSessionsAndIncrementAuthVersion() {
        LogoutAllCommand cmd = new LogoutAllCommand(userId);
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(sessionRepository.revokeAllByUserId(userId, now)).thenReturn(Mono.empty());
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(userRepository.update(any(User.class))).thenReturn(Mono.just(user.incrementAuthVersion(now)));

        StepVerifier.create(useCase.logoutAll(cmd))
                .expectNextMatches(res -> res.message().contains("All sessions revoked successfully"))
                .verifyComplete();

        verify(sessionRepository).revokeAllByUserId(userId, now);
        verify(userRepository).update(argThat(u -> u.authVersion() == 2L));
    }
}
