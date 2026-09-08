package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.RefreshToken;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenRepositoryOutputPort {
    Mono<RefreshToken> findByTokenHash(String tokenHash);
    Mono<RefreshToken> findByTokenHashForUpdate(String tokenHash);
    Mono<RefreshToken> save(RefreshToken token);
    Mono<RefreshToken> update(RefreshToken token);
    Mono<Void> revokeFamily(UUID familyId, Instant now);
    Mono<Void> revokeBySessionId(UUID sessionId, Instant now);
}
