package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.OneTimeTokenEntity;
import pe.ask.auth.output.database.entity.OutboxMessageEntity;
import pe.ask.auth.output.database.entity.RefreshTokenEntity;
import pe.ask.auth.output.database.entity.SessionEntity;
import pe.ask.auth.output.database.entity.UserEntity;
import pe.ask.auth.output.database.mapper.OneTimeTokenPersistenceMapper;
import pe.ask.auth.output.database.mapper.OutboxMessagePersistenceMapper;
import pe.ask.auth.output.database.mapper.RefreshTokenPersistenceMapper;
import pe.ask.auth.output.database.mapper.SessionPersistenceMapper;
import pe.ask.auth.output.database.mapper.UserPersistenceMapper;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entity and Mapper database tests")
class EntityAndMapperDatabaseTest {

    @Test
    @DisplayName("User mapping and entity accessors")
    void testUserMapping() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = new User(id, "raw@test.com", "raw@test.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER, Role.ROLE_ADMIN), 1L, true, "secret", now, now);

        UserEntity entity = UserPersistenceMapper.toEntity(user, true);
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getRawEmail()).isEqualTo("raw@test.com");
        assertThat(entity.getCanonicalEmail()).isEqualTo("raw@test.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getRoles()).contains("ROLE_USER");
        assertThat(entity.getAuthVersion()).isEqualTo(1L);
        assertThat(entity.getMfaEnabled()).isTrue();
        assertThat(entity.getTotpSecretEncrypted()).isEqualTo("secret");
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();

        entity.setNew(false);
        entity.setVersion(2L);
        assertThat(entity.isNew()).isFalse();
        assertThat(entity.getVersion()).isEqualTo(2L);

        User domain = UserPersistenceMapper.toDomain(entity);
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.roles()).contains(Role.ROLE_USER, Role.ROLE_ADMIN);

        UserEntity nullRolesEntity = new UserEntity(id, "raw@test.com", "raw@test.com", "hash", "ACTIVE", null, 1L, false, null, now, now, false);
        User domainWithNullRoles = UserPersistenceMapper.toDomain(nullRolesEntity);
        assertThat(domainWithNullRoles.roles()).isEmpty();

        assertThat(UserPersistenceMapper.toEntity(null, false)).isNull();
        assertThat(UserPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Session mapping and entity accessors")
    void testSessionMapping() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        Session session = new Session(id, userId, deviceId, SessionStatus.ACTIVE, "127.0.0.1", "Chrome", now, now, now.plusSeconds(3600));

        SessionEntity entity = SessionPersistenceMapper.toEntity(session, true);
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getDeviceId()).isEqualTo(deviceId);
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(entity.getUserAgent()).isEqualTo("Chrome");

        Session domain = SessionPersistenceMapper.toDomain(entity);
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.status()).isEqualTo(SessionStatus.ACTIVE);

        assertThat(SessionPersistenceMapper.toEntity(null, false)).isNull();
        assertThat(SessionPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("RefreshToken mapping and entity accessors")
    void testRefreshTokenMapping() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken(id, sessionId, familyId, userId, deviceId, "hash", RefreshTokenStatus.ACTIVE, now, now.plusSeconds(3600), null, 0L);

        RefreshTokenEntity entity = RefreshTokenPersistenceMapper.toEntity(token, true);
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getSessionId()).isEqualTo(sessionId);
        assertThat(entity.getFamilyId()).isEqualTo(familyId);
        assertThat(entity.getDeviceId()).isEqualTo(deviceId);
        assertThat(entity.getTokenHash()).isEqualTo("hash");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");

        RefreshToken domain = RefreshTokenPersistenceMapper.toDomain(entity);
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.status()).isEqualTo(RefreshTokenStatus.ACTIVE);

        assertThat(RefreshTokenPersistenceMapper.toEntity(null, false)).isNull();
        assertThat(RefreshTokenPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("OutboxMessage mapping and entity accessors")
    void testOutboxMessageMapping() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxMessage msg = new OutboxMessage(id, "User", id.toString(), "USER_CREATED", "{}", now, null, null, 0);

        OutboxMessageEntity entity = OutboxMessagePersistenceMapper.toEntity(msg, true);
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getAggregateType()).isEqualTo("User");
        assertThat(entity.getAggregateId()).isEqualTo(id.toString());
        assertThat(entity.getEventType()).isEqualTo("USER_CREATED");
        assertThat(entity.getPayload()).isEqualTo("{}");
        assertThat(entity.getAttempts()).isZero();

        OutboxMessage domain = OutboxMessagePersistenceMapper.toDomain(entity);
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.eventType()).isEqualTo("USER_CREATED");

        assertThat(OutboxMessagePersistenceMapper.toEntity(null, false)).isNull();
        assertThat(OutboxMessagePersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("OneTimeToken mapping and entity accessors")
    void testOneTimeTokenMapping() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OneTimeToken token = new OneTimeToken(id, userId, "hash", OneTimeTokenType.EMAIL_VERIFICATION, now, now.plusSeconds(1800), false, null);

        OneTimeTokenEntity entity = OneTimeTokenPersistenceMapper.toEntity(token, true);
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getTokenHash()).isEqualTo("hash");
        assertThat(entity.getType()).isEqualTo("EMAIL_VERIFICATION");

        OneTimeToken domain = OneTimeTokenPersistenceMapper.toDomain(entity);
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.type()).isEqualTo(OneTimeTokenType.EMAIL_VERIFICATION);

        assertThat(OneTimeTokenPersistenceMapper.toEntity(null, false)).isNull();
        assertThat(OneTimeTokenPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("DatabaseEnumEntity coverage")
    void testDatabaseEnumEntity() {
        for (DatabaseEnumEntity val : DatabaseEnumEntity.values()) {
            assertThat(val.value()).isNotBlank();
        }
    }
}
