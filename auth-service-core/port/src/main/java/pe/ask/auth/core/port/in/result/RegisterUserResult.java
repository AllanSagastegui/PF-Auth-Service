package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record RegisterUserResult(
        UUID userId,
        String email,
        String message
) {
    public RegisterUserResult {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(email, "email");
        DomainValidation.requireNonNull(message, "message");
    }
}
