package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.port.in.command.ForgotPasswordCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private NotificationOutputPort notificationPort;
    @Mock
    private ClockOutputPort clockPort;
    @Mock
    private IdGeneratorOutputPort idGenerator;

    private ForgotPasswordUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ForgotPasswordUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                notificationPort,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should return generic success message when user is not found to prevent user enumeration")
    void shouldReturnGenericWhenUserNotFound() {
        ForgotPasswordCommand cmd = new ForgotPasswordCommand("unknown@example.com");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("unknown@example.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.forgotPassword(cmd))
                .expectNextMatches(res -> res.message().contains("If the email is associated"))
                .verifyComplete();

        verify(notificationPort, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    @DisplayName("Should revoke existing reset tokens, generate new one, and send reset email")
    void shouldSendResetEmailWhenUserExists() {
        ForgotPasswordCommand cmd = new ForgotPasswordCommand("user@example.com");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(oneTimeTokenRepository.revokeByUserIdAndType(userId, OneTimeTokenType.PASSWORD_RESET, now)).thenReturn(Mono.empty());
        when(tokenGenerator.generateSecureToken()).thenReturn(Mono.just("reset_raw"));
        when(tokenGenerator.hashToken("reset_raw")).thenReturn(Mono.just("reset_hash"));
        when(idGenerator.nextId()).thenReturn(Mono.just(UUID.randomUUID()));
        when(oneTimeTokenRepository.save(any())).thenReturn(Mono.empty());
        when(notificationPort.sendPasswordResetEmail(eq("user@example.com"), eq("reset_raw"))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.forgotPassword(cmd))
                .expectNextMatches(res -> res.message().contains("If the email is associated"))
                .verifyComplete();

        verify(notificationPort).sendPasswordResetEmail("user@example.com", "reset_raw");
    }
}
