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
import pe.ask.auth.core.model.exception.TokenExpiredException;
import pe.ask.auth.core.port.in.command.EmailVerificationConfirmCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
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
class EmailVerificationConfirmUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private ClockOutputPort clockPort;

    private EmailVerificationConfirmUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final UUID tokenId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new EmailVerificationConfirmUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                clockPort
        );
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token does not exist")
    void shouldThrowWhenTokenNotFound() {
        EmailVerificationConfirmCommand cmd = new EmailVerificationConfirmCommand("invalid_token");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("invalid_token")).thenReturn(Mono.just("hashed_invalid"));
        when(oneTimeTokenRepository.findByTokenHashAndType("hashed_invalid", OneTimeTokenType.EMAIL_VERIFICATION))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.confirmVerification(cmd))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw TokenExpiredException when token is expired")
    void shouldThrowWhenTokenExpired() {
        EmailVerificationConfirmCommand cmd = new EmailVerificationConfirmCommand("expired_token");
        OneTimeToken expiredToken = new OneTimeToken(
                tokenId,
                userId,
                "hashed_expired",
                OneTimeTokenType.EMAIL_VERIFICATION,
                now.minusSeconds(3600),
                now.minusSeconds(10),
                false,
                null
        );

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(tokenGenerator.hashToken("expired_token")).thenReturn(Mono.just("hashed_expired"));
        when(oneTimeTokenRepository.findByTokenHashAndType("hashed_expired", OneTimeTokenType.EMAIL_VERIFICATION))
                .thenReturn(Mono.just(expiredToken));

        StepVerifier.create(useCase.confirmVerification(cmd))
                .expectError(TokenExpiredException.class)
                .verify();
    }

    @Test
    @DisplayName("Should activate user and mark token as used upon successful confirmation")
    void shouldConfirmEmailSuccessfully() {
        EmailVerificationConfirmCommand cmd = new EmailVerificationConfirmCommand("valid_token");
        OneTimeToken validToken = new OneTimeToken(
                tokenId,
                userId,
                "hashed_valid",
                OneTimeTokenType.EMAIL_VERIFICATION,
                now.minusSeconds(60),
                now.plusSeconds(1800),
                false,
                null
        );

        User pendingUser = new User(
                userId,
                "user@example.com",
                "user@example.com",
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
        when(tokenGenerator.hashToken("valid_token")).thenReturn(Mono.just("hashed_valid"));
        when(oneTimeTokenRepository.findByTokenHashAndType("hashed_valid", OneTimeTokenType.EMAIL_VERIFICATION))
                .thenReturn(Mono.just(validToken));
        when(oneTimeTokenRepository.update(any(OneTimeToken.class)))
                .thenReturn(Mono.just(validToken.markUsed(now)));
        when(userRepository.findById(userId)).thenReturn(Mono.just(pendingUser));
        when(userRepository.update(any(User.class)))
                .thenReturn(Mono.just(pendingUser.verifyEmail(now)));

        StepVerifier.create(useCase.confirmVerification(cmd))
                .expectNextMatches(res -> res.message().contains("Email verified successfully"))
                .verifyComplete();

        verify(oneTimeTokenRepository).update(argThat(OneTimeToken::used));
        verify(userRepository).update(argThat(u -> u.status() == UserStatus.ACTIVE));
    }
}
