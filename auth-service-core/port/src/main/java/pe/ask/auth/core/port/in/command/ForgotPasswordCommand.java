package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ForgotPasswordCommand(String email) {
    public ForgotPasswordCommand {
        DomainValidation.requireNonNull(email, "email");
    }
}
