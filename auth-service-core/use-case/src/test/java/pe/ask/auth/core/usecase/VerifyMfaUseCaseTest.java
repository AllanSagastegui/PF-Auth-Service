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
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.port.in.command.VerifyMfaCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyMfaUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private SessionRepositoryOutputPort sessionRepository;
    @Mock
    private RefreshTokenRepositoryOutputPort refreshTokenRepository;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private TotpOutputPort totpPort;
    @Mock
    private ClockOutputPort clockPort;
    @Mock
    private IdGeneratorOutputPort idGenerator;

    private VerifyMfaUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID deviceId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new VerifyMfaUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                totpPort,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should reject invalid or malformed MFA challenge token")
    void shouldRejectInvalidChallengeToken() {
        VerifyMfaCommand cmd = new VerifyMfaCommand("invalid_jwt", "123456", deviceId, "127.0.0.1", "Agent");
        when(tokenGenerator.verifyMfaChallengeToken("invalid_jwt")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.verifyMfa(cmd))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject invalid TOTP code")
    void shouldRejectInvalidTotpCode() {
        VerifyMfaCommand cmd = new VerifyMfaCommand("valid_jwt", "000000", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);

        when(tokenGenerator.verifyMfaChallengeToken("valid_jwt")).thenReturn(Mono.just(userId));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(totpPort.decryptSecret("enc_sec")).thenReturn(Mono.just("raw_sec"));
        when(totpPort.verifyCode("raw_sec", "000000")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.verifyMfa(cmd))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should verify valid TOTP code and issue tokens with active session")
    void shouldVerifyTotpAndIssueTokens() {
        VerifyMfaCommand cmd = new VerifyMfaCommand("valid_jwt", "123456", deviceId, "127.0.0.1", "Agent");
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, true, "enc_sec", now, now);
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID refId = UUID.randomUUID();

        when(tokenGenerator.verifyMfaChallengeToken("valid_jwt")).thenReturn(Mono.just(userId));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(totpPort.decryptSecret("enc_sec")).thenReturn(Mono.just("raw_sec"));
        when(totpPort.verifyCode("raw_sec", "123456")).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(idGenerator.nextId())
                .thenReturn(Mono.just(sessionId))
                .thenReturn(Mono.just(familyId))
                .thenReturn(Mono.just(refId));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(tokenGenerator.issueTokens(any(User.class), any(Session.class), any(UUID.class)))
                .thenReturn(Mono.just(new AuthTokens("access_token", "refresh_token", 900L, "Bearer")));
        when(tokenGenerator.hashToken("refresh_token")).thenReturn(Mono.just("ref_hash"));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.verifyMfa(cmd))
                .expectNextMatches(res ->
                        "access_token".equals(res.accessToken()) &&
                        "refresh_token".equals(res.refreshToken()) &&
                        res.expiresInSeconds() == 900L
                )
                .verifyComplete();
    }
}
