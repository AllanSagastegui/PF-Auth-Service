package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        UUID sessionId,
        UUID familyId,
        UUID userId,
        UUID deviceId,
        String tokenHash,
        RefreshTokenStatus status,
        Instant createdAt,
        Instant expiresAt,
        Instant rotatedAt,
        long sequenceNumber
) {

    public RefreshToken {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(sessionId, "sessionId");
        DomainValidation.requireNonNull(familyId, "familyId");
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(deviceId, "deviceId");
        DomainValidation.requireNonNull(tokenHash, "tokenHash");
        DomainValidation.requireNonNull(status, "status");
        DomainValidation.requireNonNull(createdAt, "createdAt");
        DomainValidation.requireNonNull(expiresAt, "expiresAt");
    }

    public boolean isUsable(Instant now) {
        return status == RefreshTokenStatus.ACTIVE && expiresAt.isAfter(now);
    }

    public RefreshToken rotate(Instant now) {
        return new RefreshToken(
                id,
                sessionId,
                familyId,
                userId,
                deviceId,
                tokenHash,
                RefreshTokenStatus.ROTATED,
                createdAt,
                expiresAt,
                now,
                sequenceNumber
        );
    }

    public RefreshToken revoke() {
        return new RefreshToken(
                id,
                sessionId,
                familyId,
                userId,
                deviceId,
                tokenHash,
                RefreshTokenStatus.REVOKED,
                createdAt,
                expiresAt,
                rotatedAt,
                sequenceNumber
        );
    }

    public RefreshToken revoke(Instant now) {
        return new RefreshToken(
                id,
                sessionId,
                familyId,
                userId,
                deviceId,
                tokenHash,
                RefreshTokenStatus.REVOKED,
                createdAt,
                expiresAt,
                now,
                sequenceNumber
        );
    }
}
