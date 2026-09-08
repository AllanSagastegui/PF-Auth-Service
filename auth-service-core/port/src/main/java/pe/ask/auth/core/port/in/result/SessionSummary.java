package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.time.Instant;
import java.util.UUID;

public record SessionSummary(
        UUID id,
        UUID deviceId,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Instant lastActivityAt,
        Instant expiresAt
) {
    public SessionSummary {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(deviceId, "deviceId");
    }
}
