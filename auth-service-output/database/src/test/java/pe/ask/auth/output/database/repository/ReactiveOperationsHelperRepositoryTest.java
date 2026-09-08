package pe.ask.auth.output.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.DatabaseOperationException;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.database.entity.UserEntity;
import pe.ask.auth.output.database.mapper.UserPersistenceMapper;
import pe.ask.core.mapper.EntityMapper;
import pe.ask.core.model.pagination.PageResponse;
import reactor.core.publisher.Flux;
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

@DisplayName("ReactiveOperationsHelperRepository tests")
class ReactiveOperationsHelperRepositoryTest {

    private UserR2dbcRepository repository;
    private TestUserRepository helper;
    private TestUserRepository unmappedHelper;

    private static class TestUserRepository extends ReactiveOperationsHelperRepository<User, UserEntity, UserR2dbcRepository> {
        TestUserRepository(UserR2dbcRepository repository, EntityMapper<User, UserEntity> mapper) {
            super(repository, mapper, null, null);
        }

        TestUserRepository(UserR2dbcRepository repository) {
            super(repository);
        }

        @Override
        public UserR2dbcRepository getRepository() {
            return super.getRepository();
        }

        @Override
        public UserEntity toEntity(User domain) {
            return super.toEntity(domain);
        }

        @Override
        public User toDomain(UserEntity entity) {
            return super.toDomain(entity);
        }
    }

    @BeforeEach
    void setUp() {
        repository = mock(UserR2dbcRepository.class);
        EntityMapper<User, UserEntity> mapper = new EntityMapper<>() {
            @Override
            public UserEntity toEntity(User domain) {
                return UserPersistenceMapper.toEntity(domain, true);
            }

            @Override
            public User toDomain(UserEntity entity) {
                return UserPersistenceMapper.toDomain(entity);
            }
        };
        helper = new TestUserRepository(repository, mapper);
        unmappedHelper = new TestUserRepository(repository);
    }

    private User createSampleUser(UUID id) {
        Instant now = Instant.now();
        return new User(id, "user@test.com", "user@test.com", "hash", UserStatus.ACTIVE, Set.of(Role.ROLE_USER), 1L, false, null, now, now);
    }

    private UserEntity createSampleEntity(UUID id) {
        Instant now = Instant.now();
        return new UserEntity(id, "user@test.com", "user@test.com", "hash", "ACTIVE", "ROLE_USER", 1L, false, null, now, now, false);
    }

    @Test
    @DisplayName("save should persist entity and return domain")
    void shouldSave() {
        UUID id = UUID.randomUUID();
        User user = createSampleUser(id);
        UserEntity entity = createSampleEntity(id);

        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(helper.save(user))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll should persist flux of entities")
    void shouldSaveAll() {
        UUID id = UUID.randomUUID();
        User user = createSampleUser(id);
        UserEntity entity = createSampleEntity(id);

        when(repository.saveAll(any(Iterable.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(helper.saveAll(Flux.just(user)))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("findById should return mapped domain")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        UserEntity entity = createSampleEntity(id);

        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(helper.findById(id))
                .assertNext(user -> assertThat(user.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("existsById should return boolean")
    void shouldExistsById() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(Mono.just(true));

        StepVerifier.create(helper.existsById(id))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("count should return count")
    void shouldCount() {
        when(repository.count()).thenReturn(Mono.just(10L));

        StepVerifier.create(helper.count())
                .expectNext(10L)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByExample should return matching domain list")
    void shouldFindByExample() {
        UUID id = UUID.randomUUID();
        User user = createSampleUser(id);
        UserEntity entity = createSampleEntity(id);

        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(helper.findByExample(user))
                .assertNext(res -> assertThat(res.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("findAll should return all domain list")
    void shouldFindAll() {
        UUID id = UUID.randomUUID();
        UserEntity entity = createSampleEntity(id);

        when(repository.findAll()).thenReturn(Flux.just(entity));

        StepVerifier.create(helper.findAll())
                .assertNext(res -> assertThat(res.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaginated with example, page, size, and sorting")
    void shouldFindPaginatedWithExample() {
        UUID id = UUID.randomUUID();
        User user = createSampleUser(id);
        UserEntity entity = createSampleEntity(id);

        when(repository.count(any(Example.class))).thenReturn(Mono.just(1L));
        when(repository.findAll(any(Example.class), any(Sort.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(helper.findPaginated(user, 0, 10, "rawEmail", "desc"))
                .assertNext((PageResponse<User> page) -> {
                    assertThat(page.getTotalElements()).isEqualTo(1L);
                    assertThat(page.getContent()).hasSize(1);
                    assertThat(page.isLast()).isTrue();
                })
                .verifyComplete();

        StepVerifier.create(helper.findPaginated(user, 0, 10))
                .assertNext(page -> assertThat(page.getTotalElements()).isEqualTo(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaginated without domain")
    void shouldFindPaginatedWithoutDomain() {
        UUID id = UUID.randomUUID();
        UserEntity entity = createSampleEntity(id);

        when(repository.count()).thenReturn(Mono.just(1L));
        when(repository.findAll()).thenReturn(Flux.just(entity));

        StepVerifier.create(helper.findPaginated(0, 10))
                .assertNext(page -> assertThat(page.getTotalElements()).isEqualTo(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("patch should update existing non-null fields")
    void shouldPatch() {
        UUID id = UUID.randomUUID();
        User patchUser = createSampleUser(id);
        UserEntity existing = createSampleEntity(id);
        UserEntity saved = createSampleEntity(id);

        when(repository.findById(id)).thenReturn(Mono.just(existing));
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(helper.patch(id, patchUser))
                .assertNext(res -> assertThat(res.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("patch should error when entity not found")
    void shouldErrorPatchWhenNotFound() {
        UUID id = UUID.randomUUID();
        User patchUser = createSampleUser(id);

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(helper.patch(id, patchUser))
                .expectError(DatabaseOperationException.class)
                .verify();
    }

    @Test
    @DisplayName("countByExample and existsByExample")
    void shouldCountAndExistsByExample() {
        User user = createSampleUser(UUID.randomUUID());

        when(repository.count(any(Example.class))).thenReturn(Mono.just(5L));
        when(repository.exists(any(Example.class))).thenReturn(Mono.just(true));

        StepVerifier.create(helper.countByExample(user))
                .expectNext(5L)
                .verifyComplete();

        StepVerifier.create(helper.existsByExample(user))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("should throw DatabaseOperationException when mapper is null")
    void shouldThrowWhenMapperNull() {
        User user = createSampleUser(UUID.randomUUID());
        UserEntity entity = createSampleEntity(UUID.randomUUID());

        assertThrows(DatabaseOperationException.class, () -> unmappedHelper.toEntity(user));
        assertThrows(DatabaseOperationException.class, () -> unmappedHelper.toDomain(entity));
    }

    @Test
    @DisplayName("should validate arguments and get repository")
    void shouldValidateArguments() {
        assertThat(helper.getRepository()).isSameAs(repository);

        User sample = createSampleUser(UUID.randomUUID());
        UUID randId = UUID.randomUUID();

        assertThrows(RequiredArgumentException.class, () -> helper.save(null));
        assertThrows(RequiredArgumentException.class, () -> helper.saveAll(null));
        assertThrows(RequiredArgumentException.class, () -> helper.findById(null));
        assertThrows(RequiredArgumentException.class, () -> helper.existsById(null));
        assertThrows(RequiredArgumentException.class, () -> helper.findByExample(null));
        assertThrows(RequiredArgumentException.class, () -> helper.findPaginated(null, 0, 10));
        assertThrows(RequiredArgumentException.class, () -> helper.patch(null, sample));
        assertThrows(RequiredArgumentException.class, () -> helper.patch(randId, null));
        assertThrows(RequiredArgumentException.class, () -> helper.countByExample(null));
        assertThrows(RequiredArgumentException.class, () -> helper.existsByExample(null));
    }
}
