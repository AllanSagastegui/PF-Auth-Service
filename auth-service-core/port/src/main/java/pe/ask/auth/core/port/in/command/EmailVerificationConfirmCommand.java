package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;

public record EmailVerificationConfirmCommand(String token) {
    public EmailVerificationConfirmCommand {
        DomainValidation.requireNonNull(token, "token");
    }
}
