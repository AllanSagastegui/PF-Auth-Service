package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record LogoutResult(String message) {
    public LogoutResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
