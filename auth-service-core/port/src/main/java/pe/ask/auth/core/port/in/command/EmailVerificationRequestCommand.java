package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;

public record EmailVerificationRequestCommand(String email) {
    public EmailVerificationRequestCommand {
        DomainValidation.requireNonNull(email, "email");
    }
}
