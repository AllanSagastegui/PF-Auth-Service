package pe.ask.auth.output.database.repository;

import org.springframework.stereotype.Repository;
import pe.ask.auth.output.database.entity.UserEntity;
import pe.ask.persistence.core.repository.R2DBCHelperRepository;
import reactor.core.publisher.Mono;

@Repository
public interface UserR2dbcRepository extends R2DBCHelperRepository<UserEntity> {
    Mono<UserEntity> findByCanonicalEmail(String canonicalEmail);
    Mono<Boolean> existsByCanonicalEmail(String canonicalEmail);
}
