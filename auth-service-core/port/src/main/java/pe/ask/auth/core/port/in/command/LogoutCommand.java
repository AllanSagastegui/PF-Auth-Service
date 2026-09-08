package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record LogoutCommand(
        String rawRefreshToken,
        UUID deviceId
) {
    public LogoutCommand {
        DomainValidation.requireNonNull(rawRefreshToken, "rawRefreshToken");
        DomainValidation.requireNonNull(deviceId, "deviceId");
    }
}
