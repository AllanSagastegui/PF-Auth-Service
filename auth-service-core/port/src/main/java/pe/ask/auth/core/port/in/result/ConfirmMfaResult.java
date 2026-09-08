package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ConfirmMfaResult(String message) {
    public ConfirmMfaResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
