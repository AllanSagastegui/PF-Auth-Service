package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface OneTimeTokenRepositoryOutputPort {
    Mono<OneTimeToken> findByTokenHashAndType(String tokenHash, OneTimeTokenType type);
    Mono<OneTimeToken> save(OneTimeToken token);
    Mono<OneTimeToken> update(OneTimeToken token);
    Mono<Void> revokeByUserIdAndType(UUID userId, OneTimeTokenType type, Instant now);
}
