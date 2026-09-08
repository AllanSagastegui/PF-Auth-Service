package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ClockOutputPort {
    Mono<Instant> now();
}
