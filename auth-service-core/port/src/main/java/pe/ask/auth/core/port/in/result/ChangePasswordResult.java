package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record ChangePasswordResult(String message) {
    public ChangePasswordResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
