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
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.command.GetMeCommand;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMeUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;

    private GetMeUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetMeUseCase(userRepository);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user is not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getMe(new GetMeCommand(userId)))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should return user profile data for authenticated user")
    void shouldReturnUserProfile() {
        User user = new User(userId, "me@example.com", "me@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER, Role.ROLE_ADMIN), 1L, true, "secret", now, now);
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.getMe(new GetMeCommand(userId)))
                .expectNextMatches(res ->
                        res.id().equals(userId) &&
                        "me@example.com".equals(res.email()) &&
                        "ACTIVE".equals(res.status()) &&
                        res.roles().contains("ROLE_USER") &&
                        res.roles().contains("ROLE_ADMIN") &&
                        res.mfaEnabled()
                )
                .verifyComplete();
    }
}
