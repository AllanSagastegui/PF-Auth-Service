package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record ChangePasswordCommand(
        UUID userId,
        String currentPassword,
        String newPassword
) {
    public ChangePasswordCommand {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(currentPassword, "currentPassword");
        DomainValidation.requireNonNull(newPassword, "newPassword");
    }
}
