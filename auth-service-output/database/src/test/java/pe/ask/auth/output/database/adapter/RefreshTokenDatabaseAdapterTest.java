package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.database.entity.RefreshTokenEntity;
import pe.ask.auth.output.database.repository.RefreshTokenR2dbcRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RefreshTokenDatabaseAdapter tests")
class RefreshTokenDatabaseAdapterTest {

    private RefreshTokenR2dbcRepository repository;
    private RefreshTokenDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenR2dbcRepository.class);
        adapter = new RefreshTokenDatabaseAdapter(repository);
    }

    @Test
    @DisplayName("findByTokenHash should return token")
    void shouldFindByTokenHash() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        RefreshTokenEntity entity = new RefreshTokenEntity(id, sessionId, familyId, userId, deviceId, "hash", "ACTIVE", now, now.plusSeconds(3600), null, 0L, false);

        when(repository.findByTokenHash("hash")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByTokenHash("hash"))
                .assertNext(token -> {
                    assertThat(token.id()).isEqualTo(id);
                    assertThat(token.tokenHash()).isEqualTo("hash");
                    assertThat(token.status()).isEqualTo(RefreshTokenStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByTokenHashForUpdate should return token")
    void shouldFindByTokenHashForUpdate() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        RefreshTokenEntity entity = new RefreshTokenEntity(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "hash", "ACTIVE", now, now.plusSeconds(3600), null, 0L, false);

        when(repository.findByTokenHashForUpdate("hash")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByTokenHashForUpdate("hash"))
                .assertNext(token -> assertThat(token.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("save and update should persist refresh token")
    void shouldSaveAndUpdate() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken(id, sessionId, familyId, userId, deviceId, "hash", RefreshTokenStatus.ACTIVE, now, now.plusSeconds(3600), null, 0L);
        RefreshTokenEntity entity = new RefreshTokenEntity(id, sessionId, familyId, userId, deviceId, "hash", "ACTIVE", now, now.plusSeconds(3600), null, 0L, false);

        when(repository.save(any(RefreshTokenEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(token))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();

        StepVerifier.create(adapter.update(token))
                .assertNext(updated -> assertThat(updated.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("revokeFamily and revokeBySessionId should call repository")
    void shouldRevoke() {
        UUID familyId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();

        when(repository.revokeFamily(familyId, now)).thenReturn(Mono.empty());
        when(repository.revokeBySessionId(sessionId, now)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.revokeFamily(familyId, now)).verifyComplete();
        StepVerifier.create(adapter.revokeBySessionId(sessionId, now)).verifyComplete();

        verify(repository).revokeFamily(familyId, now);
        verify(repository).revokeBySessionId(sessionId, now);
    }

    @Test
    @DisplayName("should validate arguments")
    void shouldValidateArguments() {
        Instant now = Instant.now();
        assertThrows(RequiredArgumentException.class, () -> adapter.findByTokenHash(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.findByTokenHashForUpdate(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.save(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.update(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.revokeFamily(null, now));
        assertThrows(RequiredArgumentException.class, () -> adapter.revokeBySessionId(null, now));
    }
}
