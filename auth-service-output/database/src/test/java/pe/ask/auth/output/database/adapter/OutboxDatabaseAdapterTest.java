package pe.ask.auth.output.database.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.database.entity.OutboxMessageEntity;
import pe.ask.auth.output.database.repository.OutboxMessageR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OutboxDatabaseAdapter tests")
class OutboxDatabaseAdapterTest {

    private OutboxMessageR2dbcRepository repository;
    private OutboxDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxMessageR2dbcRepository.class);
        adapter = new OutboxDatabaseAdapter(repository);
    }

    @Test
    @DisplayName("save should persist outbox message")
    void shouldSave() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxMessage msg = new OutboxMessage(id, "User", id.toString(), "USER_CREATED", "{}", now, null, null, 0);
        OutboxMessageEntity entity = new OutboxMessageEntity(id, "User", id.toString(), "USER_CREATED", "{}", now, null, null, 0, false);

        when(repository.save(any(OutboxMessageEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(msg))
                .assertNext(saved -> assertThat(saved.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("claimBatch should return messages")
    void shouldClaimBatch() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxMessageEntity entity = new OutboxMessageEntity(id, "User", id.toString(), "USER_CREATED", "{}", now, null, null, 0, false);

        when(repository.claimBatch(10, now)).thenReturn(Flux.just(entity));

        StepVerifier.create(adapter.claimBatch(10, now))
                .assertNext(claimed -> assertThat(claimed.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("update should persist outbox message")
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxMessage msg = new OutboxMessage(id, "User", id.toString(), "USER_CREATED", "{}", now, null, now, 0);
        OutboxMessageEntity entity = new OutboxMessageEntity(id, "User", id.toString(), "USER_CREATED", "{}", now, null, now, 0, false);

        when(repository.save(any(OutboxMessageEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.update(msg))
                .assertNext(updated -> assertThat(updated.id()).isEqualTo(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("should validate arguments")
    void shouldValidateArguments() {
        assertThrows(RequiredArgumentException.class, () -> adapter.save(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.claimBatch(10, null));
        assertThrows(RequiredArgumentException.class, () -> adapter.update(null));
    }
}
