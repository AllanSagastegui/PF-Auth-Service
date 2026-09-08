package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;

public record RegisterUserCommand(
        String email,
        String password
) {
    public RegisterUserCommand {
        DomainValidation.requireNonNull(email, "email");
        DomainValidation.requireNonNull(password, "password");
    }
}
