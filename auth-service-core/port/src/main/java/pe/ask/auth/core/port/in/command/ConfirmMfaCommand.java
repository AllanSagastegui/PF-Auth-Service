package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record ConfirmMfaCommand(
        UUID userId,
        String totpCode
) {
    public ConfirmMfaCommand {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(totpCode, "totpCode");
    }
}
