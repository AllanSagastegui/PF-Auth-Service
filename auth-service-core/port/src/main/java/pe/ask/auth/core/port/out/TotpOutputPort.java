package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

public interface TotpOutputPort {
    Mono<String> generateSecret();
    Mono<String> encryptSecret(String rawSecret);
    Mono<String> decryptSecret(String encryptedSecret);
    Mono<Boolean> verifyCode(String rawSecret, String code);
    Mono<String> generateTotpUri(String email, String secret);
}
