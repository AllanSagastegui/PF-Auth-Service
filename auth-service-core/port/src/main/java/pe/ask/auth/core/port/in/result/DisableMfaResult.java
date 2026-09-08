package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record DisableMfaResult(String message) {
    public DisableMfaResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
