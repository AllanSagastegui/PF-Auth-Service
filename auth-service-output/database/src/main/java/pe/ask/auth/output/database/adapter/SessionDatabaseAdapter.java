package pe.ask.auth.output.database.adapter;

import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.SessionEntity;
import pe.ask.auth.output.database.mapper.SessionPersistenceMapper;
import pe.ask.auth.output.database.repository.SessionR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public final class SessionDatabaseAdapter implements SessionRepositoryOutputPort {

    private final SessionR2dbcRepository repository;

    public SessionDatabaseAdapter(SessionR2dbcRepository repository) {
        this.repository = DomainValidation.requireNonNull(repository, DatabaseEnumEntity.PARAM_REPOSITORY.value());
    }

    @Override
    public Mono<Session> findById(UUID id) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        return repository.findById(id)
                .map(SessionPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Session> findByUserIdAndDeviceId(UUID userId, UUID deviceId) {
        DomainValidation.requireNonNull(userId, DatabaseEnumEntity.PARAM_USER_ID.value());
        DomainValidation.requireNonNull(deviceId, DatabaseEnumEntity.PARAM_ID.value());
        return repository.findByUserIdAndDeviceId(userId, deviceId)
                .map(SessionPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Session> findActiveByUserId(UUID userId) {
        DomainValidation.requireNonNull(userId, DatabaseEnumEntity.PARAM_USER_ID.value());
        return repository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE.name())
                .map(SessionPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Session> save(Session session) {
        DomainValidation.requireNonNull(session, DatabaseEnumEntity.PARAM_SESSION.value());
        SessionEntity entity = SessionPersistenceMapper.toEntity(session, true);
        return repository.save(entity)
                .map(SessionPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Session> update(Session session) {
        DomainValidation.requireNonNull(session, DatabaseEnumEntity.PARAM_SESSION.value());
        SessionEntity entity = SessionPersistenceMapper.toEntity(session, false);
        return repository.save(entity)
                .map(SessionPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Long> countActiveByUserId(UUID userId) {
        DomainValidation.requireNonNull(userId, DatabaseEnumEntity.PARAM_USER_ID.value());
        return repository.countByUserIdAndStatus(userId, SessionStatus.ACTIVE.name());
    }

    @Override
    public Mono<Void> revokeAllByUserId(UUID userId, Instant now) {
        DomainValidation.requireNonNull(userId, DatabaseEnumEntity.PARAM_USER_ID.value());
        return repository.revokeAllActiveByUserId(userId);
    }

    @Override
    public Mono<Void> revokeById(UUID id) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        return repository.revokeSessionById(id);
    }
}
