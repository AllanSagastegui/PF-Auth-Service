package pe.ask.auth.output.security.adapter;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.AuthTokens;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TokenGeneratorAdapter tests")
class TokenGeneratorAdapterTest {

    private TokenGeneratorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TokenGeneratorAdapter();
    }

    @Test
    @DisplayName("Should generate secure token")
    void shouldGenerateSecureToken() {
        StepVerifier.create(adapter.generateSecureToken())
                .assertNext(token -> assertThat(token).isNotBlank())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should hash token using SHA-256")
    void shouldHashToken() {
        String token = "sample-raw-refresh-token";
        StepVerifier.create(adapter.hashToken(token))
                .assertNext(hash -> assertThat(hash).isNotBlank())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should issue signed JWT access token and refresh token")
    void shouldIssueTokens() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, Instant.now(), Instant.now());
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, userId, UUID.randomUUID(), SessionStatus.ACTIVE, "127.0.0.1", "Agent", Instant.now(), Instant.now(), Instant.now().plusSeconds(3600));

        StepVerifier.create(adapter.issueTokens(user, session, UUID.randomUUID()))
                .assertNext((AuthTokens tokens) -> {
                    assertThat(tokens.accessToken()).isNotBlank();
                    assertThat(tokens.rawRefreshToken()).isNotBlank();
                    assertThat(tokens.tokenType()).isEqualTo("Bearer");
                    assertThat(tokens.expiresInSeconds()).isPositive();

                    try {
                        SignedJWT parsed = SignedJWT.parse(tokens.accessToken());
                        assertThat(parsed.getJWTClaimsSet().getSubject()).isEqualTo(userId.toString());
                        assertThat(parsed.getJWTClaimsSet().getStringClaim("sid")).isEqualTo(sessionId.toString());
                    } catch (Exception e) {
                        throw new AssertionError("Failed to parse signed JWT", e);
                    }
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create and verify MFA challenge token")
    void shouldCreateAndVerifyMfaChallenge() {
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        StepVerifier.create(adapter.createMfaChallengeToken(userId, deviceId)
                        .flatMap(challengeToken -> adapter.verifyMfaChallengeToken(challengeToken)))
                .expectNext(userId)
                .verifyComplete();

        StepVerifier.create(adapter.verifyMfaChallengeToken("non-existent-or-consumed-token"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate required arguments")
    void shouldValidateArguments() {
        assertThrows(RequiredArgumentException.class, () -> adapter.hashToken(null));

        UUID randUuid = UUID.randomUUID();
        Session session = new Session(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), SessionStatus.ACTIVE, "127.0.0.1", "Agent", Instant.now(), Instant.now(), Instant.now().plusSeconds(3600));
        assertThrows(RequiredArgumentException.class, () -> adapter.issueTokens(null, session, randUuid));

        User user = new User(UUID.randomUUID(), "user@example.com", "user@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, Instant.now(), Instant.now());
        assertThrows(RequiredArgumentException.class, () -> adapter.issueTokens(user, null, randUuid));

        assertThrows(RequiredArgumentException.class, () -> adapter.createMfaChallengeToken(null, randUuid));
        assertThrows(RequiredArgumentException.class, () -> adapter.createMfaChallengeToken(randUuid, null));
    }
}
