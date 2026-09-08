package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.database.entity.UserEntity;
import pe.ask.auth.output.database.repository.UserR2dbcRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UserDatabaseAdapter tests")
class UserDatabaseAdapterTest {

    private UserR2dbcRepository repository;
    private UserDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(UserR2dbcRepository.class);
        adapter = new UserDatabaseAdapter(repository);
    }

    @Test
    @DisplayName("findById should return user when found")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "test@example.com", "test@example.com", "hash", "ACTIVE", "ROLE_USER", 1L, false, null, Instant.now(), Instant.now(), false);

        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById(id))
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo(id);
                    assertThat(user.rawEmail()).isEqualTo("test@example.com");
                    assertThat(user.roles()).contains(Role.ROLE_USER);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByCanonicalEmail should return user when found")
    void shouldFindByCanonicalEmail() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "test@example.com", "test@example.com", "hash", "ACTIVE", "ROLE_USER", 1L, false, null, Instant.now(), Instant.now(), false);

        when(repository.findByCanonicalEmail("test@example.com")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByCanonicalEmail("test@example.com"))
                .assertNext(user -> assertThat(user.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("existsByCanonicalEmail should return boolean")
    void shouldCheckExists() {
        when(repository.existsByCanonicalEmail("test@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByCanonicalEmail("test@example.com"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("save should persist user entity")
    void shouldSaveUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "test@example.com", "test@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, Instant.now(), Instant.now());
        UserEntity savedEntity = new UserEntity(id, "test@example.com", "test@example.com", "hash", "ACTIVE", "ROLE_USER", 1L, false, null, Instant.now(), Instant.now(), false);

        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));

        StepVerifier.create(adapter.save(user))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("update should fetch existing version and save")
    void shouldUpdateUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "test@example.com", "test@example.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 2L, false, null, Instant.now(), Instant.now());
        UserEntity existing = new UserEntity(id, "test@example.com", "test@example.com", "hash", "ACTIVE", "ROLE_USER", 1L, false, null, Instant.now(), Instant.now(), false);
        existing.setVersion(5L);
        UserEntity updated = new UserEntity(id, "test@example.com", "test@example.com", "hash", "ACTIVE", "ROLE_USER", 2L, false, null, Instant.now(), Instant.now(), false);
        updated.setVersion(6L);

        when(repository.findById(id)).thenReturn(Mono.just(existing));
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(adapter.update(user))
                .assertNext(res -> assertThat(res.authVersion()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    @DisplayName("should validate arguments")
    void shouldValidateArguments() {
        assertThrows(RequiredArgumentException.class, () -> adapter.findById(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.findByCanonicalEmail(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.existsByCanonicalEmail(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.save(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.update(null));
    }
}
