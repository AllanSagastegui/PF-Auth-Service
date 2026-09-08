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
import pe.ask.auth.core.port.in.command.ConfirmMfaCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmMfaUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private TotpOutputPort totpPort;
    @Mock
    private ClockOutputPort clockPort;

    private ConfirmMfaUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ConfirmMfaUseCase(userRepository, totpPort, clockPort);
    }

    @Test
    @DisplayName("Should reject invalid TOTP code on confirmation")
    void shouldRejectInvalidTotpCode() {
        ConfirmMfaCommand cmd = new ConfirmMfaCommand(userId, "000000");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(totpPort.decryptSecret("enc_sec")).thenReturn(Mono.just("raw_sec"));
        when(totpPort.verifyCode("raw_sec", "000000")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.confirm(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should confirm MFA activation with valid TOTP code")
    void shouldConfirmMfaSuccessfully() {
        ConfirmMfaCommand cmd = new ConfirmMfaCommand(userId, "123456");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(totpPort.decryptSecret("enc_sec")).thenReturn(Mono.just("raw_sec"));
        when(totpPort.verifyCode("raw_sec", "123456")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.confirm(cmd))
                .expectNextMatches(res -> res.message().contains("activated successfully"))
                .verifyComplete();
    }
}
