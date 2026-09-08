package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IdGeneratorOutputPort {
    Mono<UUID> nextId();
}
