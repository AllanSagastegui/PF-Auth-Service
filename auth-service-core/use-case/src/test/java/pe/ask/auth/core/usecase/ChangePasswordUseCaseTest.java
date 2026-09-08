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
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.command.ChangePasswordCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
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
class ChangePasswordUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private PasswordHasherOutputPort passwordHasher;
    @Mock
    private ClockOutputPort clockPort;

    private ChangePasswordUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ChangePasswordUseCase(
                userRepository,
                passwordHasher,
                clockPort
        );
    }

    @Test
    @DisplayName("Should reject password that violates length policy")
    void shouldRejectShortPassword() {
        ChangePasswordCommand cmd = new ChangePasswordCommand(userId, "CurrentPassword123!", "Short1!");

        StepVerifier.create(useCase.changePassword(cmd))
                .expectError(PasswordPolicyException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user does not exist")
    void shouldThrowUserNotFound() {
        ChangePasswordCommand cmd = new ChangePasswordCommand(userId, "CurrentPassword123!", "NewValidSecurePassword123!");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.changePassword(cmd))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when current password does not verify")
    void shouldRejectWrongCurrentPassword() {
        ChangePasswordCommand cmd = new ChangePasswordCommand(userId, "WrongCurrentPassword123!", "NewValidSecurePassword123!");
        User user = new User(userId, "user@example.com", "user@example.com", "current_hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("WrongCurrentPassword123!", "current_hash")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.changePassword(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should change password successfully when current password verifies")
    void shouldChangePasswordSuccessfully() {
        ChangePasswordCommand cmd = new ChangePasswordCommand(userId, "CurrentPassword123!", "NewValidSecurePassword123!");
        User user = new User(userId, "user@example.com", "user@example.com", "current_hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("CurrentPassword123!", "current_hash")).thenReturn(Mono.just(true));
        when(passwordHasher.hashPassword("NewValidSecurePassword123!")).thenReturn(Mono.just("new_hash"));
        when(userRepository.update(any())).thenReturn(Mono.just(user.changePassword("new_hash", now)));

        StepVerifier.create(useCase.changePassword(cmd))
                .expectNextMatches(res -> res.message().contains("Password changed successfully"))
                .verifyComplete();

        verify(userRepository).update(argThat(u -> u.passwordHash().equals("new_hash") && u.authVersion() == 2L));
    }
}
