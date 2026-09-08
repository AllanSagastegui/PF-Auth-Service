package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

public interface NotificationOutputPort {
    Mono<Void> sendVerificationEmail(String email, String rawToken);
    Mono<Void> sendPasswordResetEmail(String email, String rawToken);
}
