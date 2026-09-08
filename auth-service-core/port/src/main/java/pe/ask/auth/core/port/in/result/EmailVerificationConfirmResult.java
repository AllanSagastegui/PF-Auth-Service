package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record EmailVerificationConfirmResult(String message) {
    public EmailVerificationConfirmResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
