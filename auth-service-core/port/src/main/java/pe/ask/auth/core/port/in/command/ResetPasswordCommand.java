package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ResetPasswordCommand(
        String token,
        String newPassword
) {
    public ResetPasswordCommand {
        DomainValidation.requireNonNull(token, "token");
        DomainValidation.requireNonNull(newPassword, "newPassword");
    }
}
