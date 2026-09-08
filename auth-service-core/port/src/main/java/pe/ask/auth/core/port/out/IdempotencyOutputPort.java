package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface IdempotencyOutputPort {
    Mono<Boolean> acquireLock(String key, String payloadHash, Duration ttl);
    Mono<String> getCachedResponse(String key);
    Mono<Void> saveCachedResponse(String key, String responseJson, Duration ttl);
}
