package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record LogoutAllCommand(UUID userId) {
    public LogoutAllCommand {
        DomainValidation.requireNonNull(userId, "userId");
    }
}
