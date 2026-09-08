package pe.ask.auth.output.database.adapter;

import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.auth.output.database.entity.RefreshTokenEntity;
import pe.ask.auth.output.database.mapper.RefreshTokenPersistenceMapper;
import pe.ask.auth.output.database.repository.RefreshTokenR2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public final class RefreshTokenDatabaseAdapter implements RefreshTokenRepositoryOutputPort {

    private final RefreshTokenR2dbcRepository repository;

    public RefreshTokenDatabaseAdapter(RefreshTokenR2dbcRepository repository) {
        this.repository = DomainValidation.requireNonNull(repository, DatabaseEnumEntity.PARAM_REPOSITORY.value());
    }

    @Override
    public Mono<RefreshToken> findByTokenHash(String tokenHash) {
        DomainValidation.requireNonNull(tokenHash, DatabaseEnumEntity.PARAM_TOKEN_HASH.value());
        return repository.findByTokenHash(tokenHash)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        DomainValidation.requireNonNull(tokenHash, DatabaseEnumEntity.PARAM_TOKEN_HASH.value());
        return repository.findByTokenHashForUpdate(tokenHash)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<RefreshToken> save(RefreshToken token) {
        DomainValidation.requireNonNull(token, DatabaseEnumEntity.PARAM_REFRESH_TOKEN.value());
        RefreshTokenEntity entity = RefreshTokenPersistenceMapper.toEntity(token, true);
        return repository.save(entity)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<RefreshToken> update(RefreshToken token) {
        DomainValidation.requireNonNull(token, DatabaseEnumEntity.PARAM_REFRESH_TOKEN.value());
        RefreshTokenEntity entity = RefreshTokenPersistenceMapper.toEntity(token, false);
        return repository.save(entity)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> revokeFamily(UUID familyId, Instant now) {
        DomainValidation.requireNonNull(familyId, DatabaseEnumEntity.PARAM_FAMILY_ID.value());
        return repository.revokeFamily(familyId, now);
    }

    @Override
    public Mono<Void> revokeBySessionId(UUID sessionId, Instant now) {
        DomainValidation.requireNonNull(sessionId, DatabaseEnumEntity.PARAM_ID.value());
        return repository.revokeBySessionId(sessionId, now);
    }
}
