package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record GetMeCommand(UUID userId) {
    public GetMeCommand {
        DomainValidation.requireNonNull(userId, "userId");
    }
}
