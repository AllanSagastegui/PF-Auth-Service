package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface RateLimiterOutputPort {
    Mono<Boolean> tryAcquire(String key, int limit, Duration window);
}
