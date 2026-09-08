package pe.ask.auth.output.security.adapter;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.ClockOutputPort;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;

public final class SystemClockAdapter implements ClockOutputPort {

    private final Clock clock;

    public SystemClockAdapter(Clock clock) {
        this.clock = DomainValidation.requireNonNull(clock, "clock");
    }

    public SystemClockAdapter() {
        this(Clock.systemUTC());
    }

    @Override
    public Mono<Instant> now() {
        return Mono.fromCallable(clock::instant);
    }
}
