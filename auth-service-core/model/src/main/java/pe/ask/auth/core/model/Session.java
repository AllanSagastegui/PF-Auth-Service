package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.UUID;

public record Session(
        UUID id,
        UUID userId,
        UUID deviceId,
        SessionStatus status,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Instant lastActivityAt,
        Instant expiresAt
) {

    public Session {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(deviceId, "deviceId");
        DomainValidation.requireNonNull(status, "status");
        DomainValidation.requireNonNull(createdAt, "createdAt");
        DomainValidation.requireNonNull(lastActivityAt, "lastActivityAt");
        DomainValidation.requireNonNull(expiresAt, "expiresAt");
    }

    public boolean isActive(Instant now) {
        return status == SessionStatus.ACTIVE && expiresAt.isAfter(now);
    }

    public Session revoke() {
        return new Session(
                id,
                userId,
                deviceId,
                SessionStatus.REVOKED,
                ipAddress,
                userAgent,
                createdAt,
                lastActivityAt,
                expiresAt
        );
    }

    public Session touch(Instant now, Instant newExpiresAt) {
        return new Session(
                id,
                userId,
                deviceId,
                status,
                ipAddress,
                userAgent,
                createdAt,
                now,
                newExpiresAt
        );
    }
}
