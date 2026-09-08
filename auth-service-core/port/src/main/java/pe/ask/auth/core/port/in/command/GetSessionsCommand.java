package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record GetSessionsCommand(UUID userId) {
    public GetSessionsCommand {
        DomainValidation.requireNonNull(userId, "userId");
    }
}
