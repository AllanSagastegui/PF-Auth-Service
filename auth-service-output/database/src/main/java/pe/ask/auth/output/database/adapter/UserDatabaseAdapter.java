package pe.ask.auth.output.database.adapter;

import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.UserEntity;
import pe.ask.auth.output.database.mapper.UserPersistenceMapper;
import pe.ask.auth.output.database.repository.UserR2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class UserDatabaseAdapter implements UserRepositoryOutputPort {

    private final UserR2dbcRepository repository;

    public UserDatabaseAdapter(UserR2dbcRepository repository) {
        this.repository = DomainValidation.requireNonNull(repository, DatabaseEnumEntity.PARAM_REPOSITORY.value());
    }

    @Override
    public Mono<User> findById(UUID id) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        return repository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Mono<User> findByCanonicalEmail(String canonicalEmail) {
        DomainValidation.requireNonNull(canonicalEmail, DatabaseEnumEntity.PARAM_CANONICAL_EMAIL.value());
        return repository.findByCanonicalEmail(canonicalEmail)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCanonicalEmail(String canonicalEmail) {
        DomainValidation.requireNonNull(canonicalEmail, DatabaseEnumEntity.PARAM_CANONICAL_EMAIL.value());
        return repository.existsByCanonicalEmail(canonicalEmail);
    }

    @Override
    public Mono<User> save(User user) {
        DomainValidation.requireNonNull(user, DatabaseEnumEntity.PARAM_USER.value());
        UserEntity entity = UserPersistenceMapper.toEntity(user, true);
        return repository.save(entity)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Mono<User> update(User user) {
        DomainValidation.requireNonNull(user, DatabaseEnumEntity.PARAM_USER.value());
        return repository.findById(user.id())
                .flatMap(existing -> {
                    UserEntity entity = UserPersistenceMapper.toEntity(user, false);
                    entity.setVersion(existing.getVersion());
                    return repository.save(entity);
                })
                .map(UserPersistenceMapper::toDomain);
    }
}
