package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record DisableMfaCommand(
        UUID userId,
        String password,
        String totpCode
) {
    public DisableMfaCommand {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(password, "password");
        DomainValidation.requireNonNull(totpCode, "totpCode");
    }
}
