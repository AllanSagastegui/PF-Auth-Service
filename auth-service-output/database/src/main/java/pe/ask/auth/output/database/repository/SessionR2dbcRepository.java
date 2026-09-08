package pe.ask.auth.output.database.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.ask.auth.output.database.entity.SessionEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SessionR2dbcRepository extends R2dbcRepository<SessionEntity, UUID> {
    Flux<SessionEntity> findByUserIdAndStatus(UUID userId, String status);
    Mono<SessionEntity> findByUserIdAndDeviceId(UUID userId, UUID deviceId);
    Mono<Long> countByUserIdAndStatus(UUID userId, String status);

    @Modifying
    @Query("UPDATE sessions SET status = 'REVOKED' WHERE user_id = :userId AND status = 'ACTIVE'")
    Mono<Void> revokeAllActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE sessions SET status = 'REVOKED' WHERE id = :id")
    Mono<Void> revokeSessionById(@Param("id") UUID id);
}
