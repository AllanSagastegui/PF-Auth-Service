package pe.ask.auth.output.database.adapter;

import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.OutboxRepositoryOutputPort;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.OutboxMessageEntity;
import pe.ask.auth.output.database.mapper.OutboxMessagePersistenceMapper;
import pe.ask.auth.output.database.repository.OutboxMessageR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public final class OutboxDatabaseAdapter implements OutboxRepositoryOutputPort {

    private final OutboxMessageR2dbcRepository repository;

    public OutboxDatabaseAdapter(OutboxMessageR2dbcRepository repository) {
        this.repository = DomainValidation.requireNonNull(repository, DatabaseEnumEntity.PARAM_REPOSITORY.value());
    }

    @Override
    public Mono<OutboxMessage> save(OutboxMessage message) {
        DomainValidation.requireNonNull(message, DatabaseEnumEntity.PARAM_OUTBOX_EVENT.value());
        OutboxMessageEntity entity = OutboxMessagePersistenceMapper.toEntity(message, true);
        return repository.save(entity)
                .map(OutboxMessagePersistenceMapper::toDomain);
    }

    @Override
    public Flux<OutboxMessage> claimBatch(int limit, Instant now) {
        DomainValidation.requireNonNull(now, DatabaseEnumEntity.PARAM_NOW.value());
        return repository.claimBatch(limit, now)
                .map(OutboxMessagePersistenceMapper::toDomain);
    }

    @Override
    public Mono<OutboxMessage> update(OutboxMessage message) {
        DomainValidation.requireNonNull(message, DatabaseEnumEntity.PARAM_OUTBOX_EVENT.value());
        OutboxMessageEntity entity = OutboxMessagePersistenceMapper.toEntity(message, false);
        return repository.save(entity)
                .map(OutboxMessagePersistenceMapper::toDomain);
    }
}
