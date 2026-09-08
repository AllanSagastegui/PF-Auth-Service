package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepositoryOutputPort {
    Mono<User> findById(UUID id);
    Mono<User> findByCanonicalEmail(String canonicalEmail);
    Mono<Boolean> existsByCanonicalEmail(String canonicalEmail);
    Mono<User> save(User user);
    Mono<User> update(User user);
}
