package pe.ask.auth.output.database.adapter;

import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.OneTimeTokenEntity;
import pe.ask.auth.output.database.mapper.OneTimeTokenPersistenceMapper;
import pe.ask.auth.output.database.repository.OneTimeTokenR2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public final class OneTimeTokenDatabaseAdapter implements OneTimeTokenRepositoryOutputPort {

    private final OneTimeTokenR2dbcRepository repository;

    public OneTimeTokenDatabaseAdapter(OneTimeTokenR2dbcRepository repository) {
        this.repository = DomainValidation.requireNonNull(repository, DatabaseEnumEntity.PARAM_REPOSITORY.value());
    }

    @Override
    public Mono<OneTimeToken> findByTokenHashAndType(String tokenHash, OneTimeTokenType type) {
        DomainValidation.requireNonNull(tokenHash, DatabaseEnumEntity.PARAM_TOKEN_HASH.value());
        DomainValidation.requireNonNull(type, DatabaseEnumEntity.PARAM_ONE_TIME_TOKEN.value());
        return repository.findByTokenHashAndType(tokenHash, type.name())
                .map(OneTimeTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<OneTimeToken> save(OneTimeToken token) {
        DomainValidation.requireNonNull(token, DatabaseEnumEntity.PARAM_ONE_TIME_TOKEN.value());
        OneTimeTokenEntity entity = OneTimeTokenPersistenceMapper.toEntity(token, true);
        return repository.save(entity)
                .map(OneTimeTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<OneTimeToken> update(OneTimeToken token) {
        DomainValidation.requireNonNull(token, DatabaseEnumEntity.PARAM_ONE_TIME_TOKEN.value());
        OneTimeTokenEntity entity = OneTimeTokenPersistenceMapper.toEntity(token, false);
        return repository.save(entity)
                .map(OneTimeTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> revokeByUserIdAndType(UUID userId, OneTimeTokenType type, Instant now) {
        DomainValidation.requireNonNull(userId, DatabaseEnumEntity.PARAM_USER_ID.value());
        DomainValidation.requireNonNull(type, DatabaseEnumEntity.PARAM_ONE_TIME_TOKEN.value());
        return repository.revokeByUserIdAndType(userId, type.name(), now);
    }
}
