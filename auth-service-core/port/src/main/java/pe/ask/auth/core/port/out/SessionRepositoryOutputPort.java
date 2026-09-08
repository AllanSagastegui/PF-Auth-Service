package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface SessionRepositoryOutputPort {
    Mono<Session> findById(UUID id);
    Mono<Session> findByUserIdAndDeviceId(UUID userId, UUID deviceId);
    Flux<Session> findActiveByUserId(UUID userId);
    Mono<Session> save(Session session);
    Mono<Session> update(Session session);
    Mono<Long> countActiveByUserId(UUID userId);
    Mono<Void> revokeAllByUserId(UUID userId, Instant now);
    Mono<Void> revokeById(UUID id);
}
