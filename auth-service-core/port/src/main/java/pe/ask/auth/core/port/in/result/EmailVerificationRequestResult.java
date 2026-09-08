package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record EmailVerificationRequestResult(String message) {
    public EmailVerificationRequestResult {
        DomainValidation.requireNonNull(message, "message");
    }
}
