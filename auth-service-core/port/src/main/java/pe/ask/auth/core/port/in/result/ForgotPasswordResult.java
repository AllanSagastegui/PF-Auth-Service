package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ForgotPasswordResult(String message) {
    public ForgotPasswordResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
