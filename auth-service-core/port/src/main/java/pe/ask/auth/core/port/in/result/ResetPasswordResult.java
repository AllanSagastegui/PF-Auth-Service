package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ResetPasswordResult(String message) {
    public ResetPasswordResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
