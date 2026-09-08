package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.OutboxMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface OutboxRepositoryOutputPort {
    Mono<OutboxMessage> save(OutboxMessage message);
    Flux<OutboxMessage> claimBatch(int limit, Instant now);
    Mono<OutboxMessage> update(OutboxMessage message);
}
