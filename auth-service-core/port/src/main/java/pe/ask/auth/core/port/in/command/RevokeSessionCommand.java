package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record RevokeSessionCommand(
        UUID userId,
        UUID sessionId
) {
    public RevokeSessionCommand {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(sessionId, "sessionId");
    }
}
