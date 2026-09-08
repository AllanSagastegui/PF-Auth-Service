package pe.ask.auth.core.port.out;

import pe.ask.auth.core.model.AuthTokens;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokenGeneratorOutputPort {
    Mono<String> generateSecureToken();
    Mono<String> hashToken(String rawToken);
    Mono<AuthTokens> issueTokens(User user, Session session, UUID familyId);
    Mono<String> createMfaChallengeToken(UUID userId, UUID deviceId);
    Mono<UUID> verifyMfaChallengeToken(String mfaChallengeToken);
}
