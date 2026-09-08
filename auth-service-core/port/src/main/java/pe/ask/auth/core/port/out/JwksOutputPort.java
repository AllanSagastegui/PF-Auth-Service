package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.JwksKey;
import reactor.core.publisher.Mono;

import java.util.List;

public interface JwksOutputPort {
    Mono<List<JwksKey>> getPublicKeys();
}
