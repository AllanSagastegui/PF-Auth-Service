package pe.ask.auth.output.security.adapter;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.IdempotencyOutputPort;
import pe.ask.auth.output.security.model.IdempotencyRecord;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class IdempotencySecurityAdapter implements IdempotencyOutputPort {

    private final ConcurrentHashMap<String, IdempotencyRecord> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> acquireLock(String key, String payloadHash, Duration ttl) {
        DomainValidation.requireNonNull(key, "key");
        DomainValidation.requireNonNull(payloadHash, "payloadHash");
        DomainValidation.requireNonNull(ttl, "ttl");

        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            IdempotencyRecord existing = storage.get(key);
            if (existing != null && existing.expiresAt().isAfter(now)) {
                return false;
            }
            storage.put(key, new IdempotencyRecord(payloadHash, now.plus(ttl)));
            return true;
        });
    }

    @Override
    public Mono<String> getCachedResponse(String key) {
        DomainValidation.requireNonNull(key, "key");

        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            IdempotencyRecord record = storage.get(key);
            if (record != null && record.expiresAt().isAfter(now) && record.value() instanceof String str) {
                return str;
            }
            return null;
        });
    }

    @Override
    public Mono<Void> saveCachedResponse(String key, String responseJson, Duration ttl) {
        DomainValidation.requireNonNull(key, "key");
        DomainValidation.requireNonNull(responseJson, "responseJson");
        DomainValidation.requireNonNull(ttl, "ttl");

        return Mono.fromRunnable(() -> {
            Instant expiresAt = Instant.now().plus(ttl);
            storage.put(key, new IdempotencyRecord(responseJson, expiresAt));
        });
    }
}
