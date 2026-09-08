package pe.ask.auth.output.database.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.ask.auth.output.database.entity.OneTimeTokenEntity;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OneTimeTokenR2dbcRepository extends R2dbcRepository<OneTimeTokenEntity, UUID> {
    Mono<OneTimeTokenEntity> findByTokenHashAndType(String tokenHash, String type);

    @Modifying
    @Query("UPDATE one_time_tokens SET used = true, used_at = :now WHERE user_id = :userId AND type = :type AND used = false")
    Mono<Void> revokeByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("now") Instant now
    );
}
