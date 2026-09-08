package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.output.database.entity.OneTimeTokenEntity;
import pe.ask.auth.output.database.repository.OneTimeTokenR2dbcRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OneTimeTokenDatabaseAdapterTest {

    private OneTimeTokenR2dbcRepository repository;
    private OneTimeTokenDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(OneTimeTokenR2dbcRepository.class);
        adapter = new OneTimeTokenDatabaseAdapter(repository);
    }

    @Test
    @DisplayName("findByTokenHashAndType should return mapped domain object when found")
    void findByTokenHashAndTypeSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OneTimeTokenEntity entity = new OneTimeTokenEntity(
                id, userId, "hash123", "EMAIL_VERIFICATION", now, now.plusSeconds(1800), false, null, false
        );

        when(repository.findByTokenHashAndType("hash123", "EMAIL_VERIFICATION"))
                .thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByTokenHashAndType("hash123", OneTimeTokenType.EMAIL_VERIFICATION))
                .assertNext(token -> {
                    assertThat(token.id()).isEqualTo(id);
                    assertThat(token.userId()).isEqualTo(userId);
                    assertThat(token.tokenHash()).isEqualTo("hash123");
                    assertThat(token.type()).isEqualTo(OneTimeTokenType.EMAIL_VERIFICATION);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("save should persist entity and return mapped domain object")
    void saveSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OneTimeToken token = new OneTimeToken(
                id, userId, "hash123", OneTimeTokenType.EMAIL_VERIFICATION, now, now.plusSeconds(1800), false, null
        );

        OneTimeTokenEntity savedEntity = new OneTimeTokenEntity(
                id, userId, "hash123", "EMAIL_VERIFICATION", now, now.plusSeconds(1800), false, null, false
        );

        when(repository.save(any(OneTimeTokenEntity.class))).thenReturn(Mono.just(savedEntity));

        StepVerifier.create(adapter.save(token))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("update should persist updated entity and return domain object")
    void updateSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OneTimeToken token = new OneTimeToken(
                id, userId, "hash123", OneTimeTokenType.EMAIL_VERIFICATION, now, now.plusSeconds(1800), true, now
        );

        OneTimeTokenEntity updatedEntity = new OneTimeTokenEntity(
                id, userId, "hash123", "EMAIL_VERIFICATION", now, now.plusSeconds(1800), true, now, false
        );

        when(repository.save(any(OneTimeTokenEntity.class))).thenReturn(Mono.just(updatedEntity));

        StepVerifier.create(adapter.update(token))
                .assertNext(updated -> assertThat(updated.used()).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("revokeByUserIdAndType should call repository with UUID and string type")
    void revokeByUserIdAndTypeSuccess() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        when(repository.revokeByUserIdAndType(userId, "EMAIL_VERIFICATION", now))
                .thenReturn(Mono.empty());

        StepVerifier.create(adapter.revokeByUserIdAndType(userId, OneTimeTokenType.EMAIL_VERIFICATION, now))
                .verifyComplete();

        verify(repository).revokeByUserIdAndType(userId, "EMAIL_VERIFICATION", now);
    }
}
