package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record RefreshTokenCommand(
        String rawRefreshToken,
        UUID deviceId,
        String idempotencyKey,
        String requestBodyHash,
        String ipAddress,
        String userAgent
) {
    public RefreshTokenCommand {
        DomainValidation.requireNonNull(rawRefreshToken, "rawRefreshToken");
        DomainValidation.requireNonNull(deviceId, "deviceId");
    }
}
