package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.database.entity.SessionEntity;
import pe.ask.auth.output.database.repository.SessionR2dbcRepository;
import reactor.core.publisher.Flux;
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

@DisplayName("SessionDatabaseAdapter tests")
class SessionDatabaseAdapterTest {

    private SessionR2dbcRepository repository;
    private SessionDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SessionR2dbcRepository.class);
        adapter = new SessionDatabaseAdapter(repository);
    }

    @Test
    @DisplayName("findById should return mapped session")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        SessionEntity entity = new SessionEntity(id, userId, deviceId, "ACTIVE", "127.0.0.1", "Agent", now, now, now.plusSeconds(3600), false);

        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById(id))
                .assertNext(session -> {
                    assertThat(session.id()).isEqualTo(id);
                    assertThat(session.status()).isEqualTo(SessionStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByUserIdAndDeviceId should return session")
    void shouldFindByUserIdAndDeviceId() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        SessionEntity entity = new SessionEntity(id, userId, deviceId, "ACTIVE", "127.0.0.1", "Agent", now, now, now.plusSeconds(3600), false);

        when(repository.findByUserIdAndDeviceId(userId, deviceId)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByUserIdAndDeviceId(userId, deviceId))
                .assertNext(session -> assertThat(session.deviceId()).isEqualTo(deviceId))
                .verifyComplete();
    }

    @Test
    @DisplayName("findActiveByUserId should return flux of sessions")
    void shouldFindActiveByUserId() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        SessionEntity entity = new SessionEntity(UUID.randomUUID(), userId, UUID.randomUUID(), "ACTIVE", "127.0.0.1", "Agent", now, now, now.plusSeconds(3600), false);

        when(repository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Flux.just(entity));

        StepVerifier.create(adapter.findActiveByUserId(userId))
                .assertNext(session -> assertThat(session.userId()).isEqualTo(userId))
                .verifyComplete();
    }

    @Test
    @DisplayName("save and update should persist session entity")
    void shouldSaveAndUpdate() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        Session session = new Session(id, userId, deviceId, SessionStatus.ACTIVE, "127.0.0.1", "Agent", now, now, now.plusSeconds(3600));
        SessionEntity entity = new SessionEntity(id, userId, deviceId, "ACTIVE", "127.0.0.1", "Agent", now, now, now.plusSeconds(3600), false);

        when(repository.save(any(SessionEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(session))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();

        StepVerifier.create(adapter.update(session))
                .assertNext(updated -> assertThat(updated.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("countActiveByUserId should return count")
    void shouldCountActive() {
        UUID userId = UUID.randomUUID();
        when(repository.countByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countActiveByUserId(userId))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("revokeAllByUserId and revokeById should call repository")
    void shouldRevoke() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        when(repository.revokeAllActiveByUserId(userId)).thenReturn(Mono.empty());
        when(repository.revokeSessionById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.revokeAllByUserId(userId, now)).verifyComplete();
        StepVerifier.create(adapter.revokeById(id)).verifyComplete();

        verify(repository).revokeAllActiveByUserId(userId);
        verify(repository).revokeSessionById(id);
    }

    @Test
    @DisplayName("should validate arguments")
    void shouldValidateArguments() {
        UUID randId = UUID.randomUUID();
        Instant now = Instant.now();
        assertThrows(RequiredArgumentException.class, () -> adapter.findById(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.findByUserIdAndDeviceId(null, randId));
        assertThrows(RequiredArgumentException.class, () -> adapter.findByUserIdAndDeviceId(randId, null));
        assertThrows(RequiredArgumentException.class, () -> adapter.findActiveByUserId(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.save(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.update(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.countActiveByUserId(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.revokeAllByUserId(null, now));
        assertThrows(RequiredArgumentException.class, () -> adapter.revokeById(null));
    }
}
