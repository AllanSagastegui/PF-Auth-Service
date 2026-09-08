package pe.ask.auth.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.constant.DomainEventEnum;
import pe.ask.auth.core.model.constant.DomainFieldEnum;
import pe.ask.auth.core.model.constant.DomainMessageEnum;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.RequiredArgumentException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Domain Models and Enums Tests")
class DomainModelAndEnumsTest {

    @Test
    @DisplayName("Test AuthTokens")
    void testAuthTokens() {
        AuthTokens tokens = AuthTokens.ofBearer("access-tok", "refresh-tok", 900L);
        assertThat(tokens.accessToken()).isEqualTo("access-tok");
        assertThat(tokens.rawRefreshToken()).isEqualTo("refresh-tok");
        assertThat(tokens.tokenType()).isEqualTo("Bearer");
        assertThat(tokens.expiresInSeconds()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Test Role enum")
    void testRoleEnum() {
        assertThat(Role.ROLE_USER.name()).isEqualTo("ROLE_USER");
        assertThat(Role.ROLE_ADMIN.name()).isEqualTo("ROLE_ADMIN");
        assertThat(Role.valueOf("ROLE_USER")).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Test User properties and validation")
    void testUserProperties() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = new User(
                id,
                "john@example.com",
                "john@example.com",
                "hashedPass",
                UserStatus.ACTIVE,
                Set.of(Role.ROLE_USER),
                1L,
                false,
                null,
                now,
                now
        );

        assertThat(user.id()).isEqualTo(id);
        assertThat(user.rawEmail()).isEqualTo("john@example.com");
        assertThat(user.canonicalEmail()).isEqualTo("john@example.com");
        assertThat(user.passwordHash()).isEqualTo("hashedPass");
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.roles()).contains(Role.ROLE_USER);
        assertThat(user.authVersion()).isEqualTo(1L);
        assertThat(user.mfaEnabled()).isFalse();
        assertThat(user.totpSecretEncrypted()).isNull();
        assertThat(user.createdAt()).isEqualTo(now);
        assertThat(user.updatedAt()).isEqualTo(now);
        assertThat(user.canAuthenticate()).isTrue();

        assertThat(User.canonicalizeEmail("  JOHN@EXAMPLE.COM ")).isEqualTo("john@example.com");
        assertThat(User.canonicalizeEmail(null)).isNull();
    }

    @Test
    @DisplayName("Test User mutations")
    void testUserMutations() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = new User(id, "p@ex.com", "p@ex.com", "h", UserStatus.PENDING_VERIFICATION, Set.of(), 1L, false, null, now, now);
        assertThat(user.canAuthenticate()).isFalse();

        User verified = user.verifyEmail(now);
        assertThat(verified.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(verified.verifyEmail(now)).isSameAs(verified);

        User changedPass = verified.changePassword("newHash", now);
        assertThat(changedPass.passwordHash()).isEqualTo("newHash");
        assertThat(changedPass.authVersion()).isEqualTo(2L);

        User incVer = verified.incrementAuthVersion(now);
        assertThat(incVer.authVersion()).isEqualTo(2L);

        User enabledMfa = verified.enableMfa("encSecret", now);
        assertThat(enabledMfa.mfaEnabled()).isTrue();
        assertThat(enabledMfa.totpSecretEncrypted()).isEqualTo("encSecret");

        User disabledMfa = enabledMfa.disableMfa(now);
        assertThat(disabledMfa.mfaEnabled()).isFalse();
        assertThat(disabledMfa.totpSecretEncrypted()).isNull();
    }

    @Test
    @DisplayName("Test UserStatus enum values")
    void testUserStatusEnum() {
        assertThat(UserStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(UserStatus.PENDING_VERIFICATION.name()).isEqualTo("PENDING_VERIFICATION");
        assertThat(UserStatus.LOCKED.name()).isEqualTo("LOCKED");
        assertThat(UserStatus.DISABLED.name()).isEqualTo("DISABLED");
        assertThat(UserStatus.DELETED.name()).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("Test Session and SessionStatus")
    void testSessionAndStatus() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);

        Session session = new Session(
                id,
                userId,
                deviceId,
                SessionStatus.ACTIVE,
                "127.0.0.1",
                "Mozilla/5.0",
                now,
                now,
                expiresAt
        );

        assertThat(session.id()).isEqualTo(id);
        assertThat(session.userId()).isEqualTo(userId);
        assertThat(session.deviceId()).isEqualTo(deviceId);
        assertThat(session.status()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.ipAddress()).isEqualTo("127.0.0.1");
        assertThat(session.userAgent()).isEqualTo("Mozilla/5.0");
        assertThat(session.expiresAt()).isEqualTo(expiresAt);
        assertThat(session.isActive(now)).isTrue();

        Session revoked = session.revoke();
        assertThat(revoked.status()).isEqualTo(SessionStatus.REVOKED);
        assertThat(revoked.isActive(now)).isFalse();

        Instant newExpiry = expiresAt.plusSeconds(1800);
        Session touched = session.touch(now, newExpiry);
        assertThat(touched.expiresAt()).isEqualTo(newExpiry);

        assertThat(SessionStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(SessionStatus.REVOKED.name()).isEqualTo("REVOKED");
        assertThat(SessionStatus.EXPIRED.name()).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("Test RefreshToken and RefreshTokenStatus")
    void testRefreshTokenAndStatus() {
        UUID id = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(604800);

        RefreshToken token = new RefreshToken(
                id,
                sessionId,
                familyId,
                userId,
                deviceId,
                "tokenHash123",
                RefreshTokenStatus.ACTIVE,
                now,
                expiresAt,
                null,
                1L
        );

        assertThat(token.id()).isEqualTo(id);
        assertThat(token.sessionId()).isEqualTo(sessionId);
        assertThat(token.familyId()).isEqualTo(familyId);
        assertThat(token.userId()).isEqualTo(userId);
        assertThat(token.deviceId()).isEqualTo(deviceId);
        assertThat(token.tokenHash()).isEqualTo("tokenHash123");
        assertThat(token.status()).isEqualTo(RefreshTokenStatus.ACTIVE);
        assertThat(token.isUsable(now)).isTrue();

        RefreshToken rotated = token.rotate(now);
        assertThat(rotated.status()).isEqualTo(RefreshTokenStatus.ROTATED);
        assertThat(rotated.rotatedAt()).isEqualTo(now);
        assertThat(rotated.isUsable(now)).isFalse();

        RefreshToken revoked1 = token.revoke();
        assertThat(revoked1.status()).isEqualTo(RefreshTokenStatus.REVOKED);

        RefreshToken revoked2 = token.revoke(now);
        assertThat(revoked2.status()).isEqualTo(RefreshTokenStatus.REVOKED);
        assertThat(revoked2.rotatedAt()).isEqualTo(now);

        assertThat(RefreshTokenStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(RefreshTokenStatus.ROTATED.name()).isEqualTo("ROTATED");
        assertThat(RefreshTokenStatus.REVOKED.name()).isEqualTo("REVOKED");
    }

    @Test
    @DisplayName("Test OneTimeToken and OneTimeTokenType")
    void testOneTimeTokenAndType() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(900);

        OneTimeToken ott = new OneTimeToken(
                id,
                userId,
                "ottHash",
                OneTimeTokenType.EMAIL_VERIFICATION,
                now,
                expiresAt,
                false,
                null
        );

        assertThat(ott.id()).isEqualTo(id);
        assertThat(ott.userId()).isEqualTo(userId);
        assertThat(ott.tokenHash()).isEqualTo("ottHash");
        assertThat(ott.type()).isEqualTo(OneTimeTokenType.EMAIL_VERIFICATION);
        assertThat(ott.used()).isFalse();
        assertThat(ott.isUsable(now)).isTrue();

        OneTimeToken used = ott.markUsed(now);
        assertThat(used.used()).isTrue();
        assertThat(used.usedAt()).isEqualTo(now);
        assertThat(used.isUsable(now)).isFalse();

        assertThat(OneTimeTokenType.EMAIL_VERIFICATION.name()).isEqualTo("EMAIL_VERIFICATION");
        assertThat(OneTimeTokenType.PASSWORD_RESET.name()).isEqualTo("PASSWORD_RESET");
    }

    @Test
    @DisplayName("Test TotpSecret")
    void testTotpSecret() {
        UUID userId = UUID.randomUUID();
        TotpSecret secret = new TotpSecret(userId, "encKey", List.of("code1", "code2"), true);
        assertThat(secret.userId()).isEqualTo(userId);
        assertThat(secret.secretEncrypted()).isEqualTo("encKey");
        assertThat(secret.recoveryCodesHashed()).containsExactly("code1", "code2");
        assertThat(secret.confirmed()).isTrue();

        TotpSecret defaultSecret = new TotpSecret(userId, "encKey", null, false);
        assertThat(defaultSecret.recoveryCodesHashed()).isEmpty();
    }

    @Test
    @DisplayName("Test OutboxMessage")
    void testOutboxMessage() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxMessage msg = new OutboxMessage(
                id,
                "User",
                id.toString(),
                "UserRegistered",
                "{\"userId\":\"" + id + "\"}",
                now,
                null,
                null,
                0
        );

        assertThat(msg.id()).isEqualTo(id);
        assertThat(msg.aggregateType()).isEqualTo("User");
        assertThat(msg.aggregateId()).isEqualTo(id.toString());
        assertThat(msg.eventType()).isEqualTo("UserRegistered");
        assertThat(msg.payload()).contains("userId");
        assertThat(msg.retryCount()).isZero();

        OutboxMessage processed = msg.markProcessed(now);
        assertThat(processed.processedAt()).isEqualTo(now);

        Instant nextAttempt = now.plusSeconds(60);
        OutboxMessage retried = msg.incrementRetry(nextAttempt);
        assertThat(retried.retryCount()).isEqualTo(1);
        assertThat(retried.nextAttemptAt()).isEqualTo(nextAttempt);
    }

    @Test
    @DisplayName("Test JwksKey")
    void testJwksKey() {
        JwksKey key = new JwksKey("EC", "P-256", "key-1", "sig", "ES256", "x-coord", "y-coord");
        assertThat(key.kid()).isEqualTo("key-1");
        assertThat(key.kty()).isEqualTo("EC");
        assertThat(key.use()).isEqualTo("sig");
        assertThat(key.alg()).isEqualTo("ES256");
        assertThat(key.crv()).isEqualTo("P-256");
        assertThat(key.x()).isEqualTo("x-coord");
        assertThat(key.y()).isEqualTo("y-coord");
    }

    @Test
    @DisplayName("Test DomainValidation")
    void testDomainValidation() {
        DomainValidation.requireNonNull("test", "testParam");

        assertThatThrownBy(() -> DomainValidation.requireNonNull(null, "paramName"))
                .isInstanceOf(RequiredArgumentException.class)
                .hasMessageContaining("paramName");
    }

    @Test
    @DisplayName("Test Domain Constants Enums")
    void testDomainConstantsEnums() {
        for (DomainMessageEnum e : DomainMessageEnum.values()) {
            assertThat(e.value()).isNotBlank();
        }
        for (DomainEventEnum e : DomainEventEnum.values()) {
            assertThat(e.value()).isNotBlank();
        }
        for (DomainFieldEnum e : DomainFieldEnum.values()) {
            assertThat(e.value()).isNotBlank();
        }
    }
}
