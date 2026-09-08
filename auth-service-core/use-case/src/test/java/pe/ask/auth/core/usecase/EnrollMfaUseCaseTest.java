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
import pe.ask.auth.core.port.in.command.EnrollMfaCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
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
class EnrollMfaUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private TotpOutputPort totpPort;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private ClockOutputPort clockPort;

    private EnrollMfaUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new EnrollMfaUseCase(
                userRepository,
                totpPort,
                tokenGenerator,
                clockPort
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        EnrollMfaCommand cmd = new EnrollMfaCommand(userId);
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(cmd))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should generate secret, QR URI, recovery codes, and update user")
    void shouldEnrollMfaSuccessfully() {
        EnrollMfaCommand cmd = new EnrollMfaCommand(userId);
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(totpPort.generateSecret()).thenReturn(Mono.just("JBSWY3DPEHPK3PXP"));
        when(totpPort.encryptSecret("JBSWY3DPEHPK3PXP")).thenReturn(Mono.just("enc_secret"));
        when(tokenGenerator.generateSecureToken()).thenReturn(Mono.just("ABCDEFGHIJ1234567890"));
        when(totpPort.generateTotpUri("user@example.com", "JBSWY3DPEHPK3PXP"))
                .thenReturn(Mono.just("otpauth://totp/Ask:user@example.com?secret=JBSWY3DPEHPK3PXP"));
        when(userRepository.update(any())).thenReturn(Mono.just(user.enableMfa("enc_secret", now)));

        StepVerifier.create(useCase.enroll(cmd))
                .expectNextMatches(res ->
                        "JBSWY3DPEHPK3PXP".equals(res.secret()) &&
                        res.qrCodeUri().contains("otpauth://totp/") &&
                        res.recoveryCodes().size() == 8
                )
                .verifyComplete();

        verify(userRepository).update(argThat(u -> u.mfaEnabled() && "enc_secret".equals(u.totpSecretEncrypted())));
    }
}
