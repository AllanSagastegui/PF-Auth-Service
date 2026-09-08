package pe.ask.auth.output.database.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.ask.auth.output.database.entity.RefreshTokenEntity;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RefreshTokenR2dbcRepository extends R2dbcRepository<RefreshTokenEntity, UUID> {
    Mono<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Query("SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash FOR UPDATE")
    Mono<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("UPDATE refresh_tokens SET status = 'REVOKED', revoked_at = :now WHERE family_id = :familyId")
    Mono<Void> revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE refresh_tokens SET status = 'REVOKED', revoked_at = :now WHERE session_id = :sessionId")
    Mono<Void> revokeBySessionId(@Param("sessionId") UUID sessionId, @Param("now") Instant now);
}
