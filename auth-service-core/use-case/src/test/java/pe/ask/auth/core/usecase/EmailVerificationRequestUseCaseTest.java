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
import pe.ask.auth.core.port.in.command.EmailVerificationRequestCommand;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationRequestUseCaseTest {

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

    private EmailVerificationRequestUseCase useCase;

    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final UUID tokenId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new EmailVerificationRequestUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                notificationPort,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should return generic success message when user does not exist to prevent enumeration")
    void shouldReturnGenericSuccessWhenUserNotFound() {
        EmailVerificationRequestCommand cmd = new EmailVerificationRequestCommand("nonexistent@example.com");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("nonexistent@example.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.requestVerification(cmd))
                .expectNextMatches(result -> result.message().contains("If the account exists"))
                .verifyComplete();

        verify(tokenGenerator, never()).generateSecureToken();
        verify(notificationPort, never()).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("Should return generic success without action when user is already active")
    void shouldDoNothingWhenUserAlreadyActive() {
        EmailVerificationRequestCommand cmd = new EmailVerificationRequestCommand("active@example.com");
        User activeUser = new User(
                userId,
                "active@example.com",
                "active@example.com",
                "hash",
                UserStatus.ACTIVE,
                Set.of(Role.ROLE_USER),
                1L,
                false,
                null,
                now,
                now
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("active@example.com")).thenReturn(Mono.just(activeUser));

        StepVerifier.create(useCase.requestVerification(cmd))
                .expectNextMatches(result -> result.message().contains("If the account exists"))
                .verifyComplete();

        verify(tokenGenerator, never()).generateSecureToken();
        verify(notificationPort, never()).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("Should revoke existing tokens, issue new one, and dispatch email when user is pending")
    void shouldIssueNewVerificationTokenWhenPending() {
        EmailVerificationRequestCommand cmd = new EmailVerificationRequestCommand("Pending@Example.com");
        User pendingUser = new User(
                userId,
                "Pending@Example.com",
                "pending@example.com",
                "hash",
                UserStatus.PENDING_VERIFICATION,
                Set.of(Role.ROLE_USER),
                1L,
                false,
                null,
                now,
                now
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("pending@example.com")).thenReturn(Mono.just(pendingUser));
        when(oneTimeTokenRepository.revokeByUserIdAndType(userId, OneTimeTokenType.EMAIL_VERIFICATION, now))
                .thenReturn(Mono.empty());
        when(tokenGenerator.generateSecureToken()).thenReturn(Mono.just("new_raw_token"));
        when(tokenGenerator.hashToken("new_raw_token")).thenReturn(Mono.just("new_hashed_token"));
        when(idGenerator.nextId()).thenReturn(Mono.just(tokenId));
        when(oneTimeTokenRepository.save(any())).thenReturn(Mono.empty());
        when(notificationPort.sendVerificationEmail("Pending@Example.com", "new_raw_token")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.requestVerification(cmd))
                .expectNextMatches(result -> result.message().contains("If the account exists"))
                .verifyComplete();

        verify(oneTimeTokenRepository).revokeByUserIdAndType(userId, OneTimeTokenType.EMAIL_VERIFICATION, now);
        verify(notificationPort).sendVerificationEmail("Pending@Example.com", "new_raw_token");
    }
}
