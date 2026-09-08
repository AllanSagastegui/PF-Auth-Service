package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

public interface PasswordHasherOutputPort {
    Mono<String> hashPassword(String rawPassword);
    Mono<Boolean> verifyPassword(String rawPassword, String passwordHash);
    Mono<Void> verifyDummyPassword(String rawPassword);
}
