package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record LogoutAllResult(String message) {
    public LogoutAllResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
