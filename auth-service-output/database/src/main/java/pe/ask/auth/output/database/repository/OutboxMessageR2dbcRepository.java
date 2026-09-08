package pe.ask.auth.output.database.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.ask.auth.output.database.entity.OutboxMessageEntity;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OutboxMessageR2dbcRepository extends R2dbcRepository<OutboxMessageEntity, UUID> {

    @Query("SELECT * FROM outbox_messages WHERE published_at IS NULL AND next_attempt_at <= :now ORDER BY created_at ASC LIMIT :limit")
    Flux<OutboxMessageEntity> claimBatch(@Param("limit") int limit, @Param("now") Instant now);
}
