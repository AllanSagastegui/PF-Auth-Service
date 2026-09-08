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
import pe.ask.auth.core.port.in.command.DisableMfaCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
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
class DisableMfaUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private PasswordHasherOutputPort passwordHasher;
    @Mock
    private TotpOutputPort totpPort;
    @Mock
    private ClockOutputPort clockPort;

    private DisableMfaUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new DisableMfaUseCase(userRepository, passwordHasher, totpPort, clockPort);
    }

    @Test
    @DisplayName("Should reject MFA disable if password is wrong")
    void shouldRejectWrongPassword() {
        DisableMfaCommand cmd = new DisableMfaCommand(userId, "WrongPassword123!", "123456");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("WrongPassword123!", "hash")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.disable(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should disable MFA when password and TOTP code match")
    void shouldDisableMfaSuccessfully() {
        DisableMfaCommand cmd = new DisableMfaCommand(userId, "CorrectPassword123!", "123456");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("CorrectPassword123!", "hash")).thenReturn(Mono.just(true));
        when(totpPort.decryptSecret("enc_sec")).thenReturn(Mono.just("raw_sec"));
        when(totpPort.verifyCode("raw_sec", "123456")).thenReturn(Mono.just(true));
        when(userRepository.update(any(User.class))).thenReturn(Mono.just(user.disableMfa(now)));

        StepVerifier.create(useCase.disable(cmd))
                .expectNextMatches(res -> res.message().contains("disabled"))
                .verifyComplete();

        verify(userRepository).update(argThat(u -> !u.mfaEnabled() && u.totpSecretEncrypted() == null));
    }
}
