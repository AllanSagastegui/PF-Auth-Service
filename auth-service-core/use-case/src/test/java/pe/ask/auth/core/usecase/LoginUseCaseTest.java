package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.AuthTokens;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.AccountDisabledException;
import pe.ask.auth.core.model.exception.AccountLockedException;
import pe.ask.auth.core.model.exception.EmailNotVerifiedException;
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.port.in.command.LoginCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private RefreshTokenRepositoryOutputPort refreshTokenRepository;
    @Mock
    private PasswordHasherOutputPort passwordHasher;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private ClockOutputPort clockPort;
    @Mock
    private IdGeneratorOutputPort idGenerator;

    private LoginUseCase useCase;
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final UUID deviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                passwordHasher,
                tokenGenerator,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should run dummy password verification when user is not found to mitigate timing attacks")
    void shouldRunDummyVerificationWhenUserNotFound() {
        LoginCommand cmd = new LoginCommand("unknown@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("unknown@example.com")).thenReturn(Mono.empty());
        when(passwordHasher.verifyDummyPassword("Password123!")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.login(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(passwordHasher).verifyDummyPassword("Password123!");
    }

    @Test
    @DisplayName("Should reject login when password does not match")
    void shouldRejectInvalidPassword() {
        LoginCommand cmd = new LoginCommand("user@example.com", "WrongPassword1!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("WrongPassword1!", "hash")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.login(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject login when account is locked")
    void shouldRejectLockedAccount() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.LOCKED, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("Password123!", "hash")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.login(cmd))
                .expectError(AccountLockedException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject login when account is disabled")
    void shouldRejectDisabledAccount() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.DISABLED, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("Password123!", "hash")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.login(cmd))
                .expectError(AccountDisabledException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject login when email is not verified")
    void shouldRejectUnverifiedAccount() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.PENDING_VERIFICATION, Set.of(Role.ROLE_USER), 1L, false, null, now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("Password123!", "hash")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.login(cmd))
                .expectError(EmailNotVerifiedException.class)
                .verify();
    }

    @Test
    @DisplayName("Should return MFA required response when MFA is enabled on account")
    void shouldRequireMfaWhenEnabled() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "secret", now, now);

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("Password123!", "hash")).thenReturn(Mono.just(true));
        when(tokenGenerator.createMfaChallengeToken(userId, deviceId)).thenReturn(Mono.just("mfa_challenge_jwt"));

        StepVerifier.create(useCase.login(cmd))
                .expectNextMatches(res -> res.mfaRequired() && "mfa_challenge_jwt".equals(res.mfaChallengeToken()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should authenticate successfully and issue tokens without MFA")
    void shouldLoginSuccessfully() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123!", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID refId = UUID.randomUUID();

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.findByCanonicalEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordHasher.verifyPassword("Password123!", "hash")).thenReturn(Mono.just(true));
        when(sessionRepository.countActiveByUserId(userId)).thenReturn(Mono.just(1L));
        when(idGenerator.nextId())
                .thenReturn(Mono.just(sessionId))
                .thenReturn(Mono.just(familyId))
                .thenReturn(Mono.just(refId));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(tokenGenerator.issueTokens(any(User.class), any(Session.class), any(UUID.class)))
                .thenReturn(Mono.just(new AuthTokens("access_token", "refresh_token", 900L, "Bearer")));
        when(tokenGenerator.hashToken("refresh_token")).thenReturn(Mono.just("ref_hash"));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.login(cmd))
                .expectNextMatches(res ->
                        !res.mfaRequired() &&
                        "access_token".equals(res.accessToken()) &&
                        "refresh_token".equals(res.refreshToken()) &&
                        res.expiresInSeconds() == 900L
                )
                .verifyComplete();
    }
}
