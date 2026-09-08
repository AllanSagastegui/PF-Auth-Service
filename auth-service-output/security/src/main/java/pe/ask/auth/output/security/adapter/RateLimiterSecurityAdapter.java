package pe.ask.auth.output.security.adapter;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.RateLimiterOutputPort;
import pe.ask.auth.output.security.model.RateLimiterCounter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RateLimiterSecurityAdapter implements RateLimiterOutputPort {

    private final ConcurrentHashMap<String, RateLimiterCounter> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> tryAcquire(String key, int limit, Duration window) {
        DomainValidation.requireNonNull(key, "key");
        DomainValidation.requireNonNull(window, "window");

        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            RateLimiterCounter counter = counters.compute(key, (k, existing) -> {
                if (existing == null || existing.windowExpiresAt().isBefore(now)) {
                    return new RateLimiterCounter(new AtomicInteger(1), now.plus(window));
                }
                existing.count().incrementAndGet();
                return existing;
            });
            return counter.count().get() <= limit;
        });
    }
}
