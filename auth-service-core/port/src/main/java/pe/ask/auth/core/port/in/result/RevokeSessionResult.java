package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record RevokeSessionResult(String message) {
    public RevokeSessionResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
