package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record EnrollMfaCommand(UUID userId) {
    public EnrollMfaCommand {
        DomainValidation.requireNonNull(userId, "userId");
    }
}
