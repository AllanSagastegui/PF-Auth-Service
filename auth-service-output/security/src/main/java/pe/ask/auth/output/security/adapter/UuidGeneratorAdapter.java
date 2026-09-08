package pe.ask.auth.output.security.adapter;

import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class UuidGeneratorAdapter implements IdGeneratorOutputPort {

    @Override
    public Mono<UUID> nextId() {
        return Mono.fromCallable(UUID::randomUUID);
    }
}
