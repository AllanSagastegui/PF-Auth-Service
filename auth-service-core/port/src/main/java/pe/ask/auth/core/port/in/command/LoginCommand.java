package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record LoginCommand(
        String email,
        String password,
        UUID deviceId,
        String ipAddress,
        String userAgent
) {
    public LoginCommand {
        DomainValidation.requireNonNull(email, "email");
        DomainValidation.requireNonNull(password, "password");
        DomainValidation.requireNonNull(deviceId, "deviceId");
    }
}
