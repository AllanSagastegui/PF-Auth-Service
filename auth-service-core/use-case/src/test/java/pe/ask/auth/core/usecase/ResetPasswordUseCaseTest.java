package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.port.in.command.ResetPasswordCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
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
class ResetPasswordUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    @Mock
    private PasswordHasherOutputPort passwordHasher;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private ClockOutputPort clockPort;

    private ResetPasswordUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final UUID tokenId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(
                userRepository,
                sessionRepository,
                oneTimeTokenRepository,
                passwordHasher,
                tokenGenerator,
                clockPort
        );
    }

    @Test
    @DisplayName("Should reject password shorter than 15 characters")
    void shouldRejectShortPassword() {
        ResetPasswordCommand cmd = new ResetPasswordCommand("valid_token", "Short123!");

        StepVerifier.create(useCase.resetPassword(cmd))
                .expectError(PasswordPolicyException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject invalid or nonexistent token")
    void shouldRejectInvalidToken() {
        ResetPasswordCommand cmd = new ResetPasswordCommand("invalid_tok", "NewSecurePassword12345!");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("invalid_tok")).thenReturn(Mono.just("hash_val"));
        when(oneTimeTokenRepository.findByTokenHashAndType("hash_val", OneTimeTokenType.PASSWORD_RESET))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.resetPassword(cmd))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reset password, revoke all active sessions, and increment auth version")
    void shouldResetPasswordSuccessfully() {
        ResetPasswordCommand cmd = new ResetPasswordCommand("valid_token", "NewSecurePassword12345!");
        OneTimeToken token = new OneTimeToken(
                tokenId,
                userId,
                "hash_val",
                OneTimeTokenType.PASSWORD_RESET,
                now.minusSeconds(60),
                now.plusSeconds(900),
                false,
                null
        );
        User user = new User(userId, "user@example.com", "user@example.com", "old_hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("valid_token")).thenReturn(Mono.just("hash_val"));
        when(oneTimeTokenRepository.findByTokenHashAndType("hash_val", OneTimeTokenType.PASSWORD_RESET))
                .thenReturn(Mono.just(token));
        when(oneTimeTokenRepository.update(any())).thenReturn(Mono.just(token.markUsed(now)));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordHasher.hashPassword("NewSecurePassword12345!")).thenReturn(Mono.just("new_hash"));
        when(userRepository.update(any())).thenReturn(Mono.just(user.changePassword("new_hash", now)));
        when(sessionRepository.revokeAllByUserId(userId, now)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.resetPassword(cmd))
                .expectNextMatches(res -> res.message().contains("Password reset successfully"))
                .verifyComplete();

        verify(oneTimeTokenRepository).update(argThat(OneTimeToken::used));
        verify(userRepository).update(argThat(u -> u.passwordHash().equals("new_hash") && u.authVersion() == 2L));
        verify(sessionRepository).revokeAllByUserId(userId, now);
    }
}
