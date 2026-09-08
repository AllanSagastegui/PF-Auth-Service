package pe.ask.auth.output.security.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.UUID;

public record MfaChallenge(
        UUID userId,
        UUID deviceId,
        Instant expiresAt
) {
    public MfaChallenge {
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(deviceId, "deviceId");
        DomainValidation.requireNonNull(expiresAt, "expiresAt");
    }
}
