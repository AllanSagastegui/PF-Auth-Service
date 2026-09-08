package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.UUID;

public record OneTimeToken(
        UUID id,
        UUID userId,
        String tokenHash,
        OneTimeTokenType type,
        Instant createdAt,
        Instant expiresAt,
        boolean used,
        Instant usedAt
) {

    public OneTimeToken {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(userId, "userId");
        DomainValidation.requireNonNull(tokenHash, "tokenHash");
        DomainValidation.requireNonNull(type, "type");
        DomainValidation.requireNonNull(createdAt, "createdAt");
        DomainValidation.requireNonNull(expiresAt, "expiresAt");
    }

    public boolean isUsable(Instant now) {
        return !used && expiresAt.isAfter(now);
    }

    public OneTimeToken markUsed(Instant now) {
        return new OneTimeToken(
                id,
                userId,
                tokenHash,
                type,
                createdAt,
                expiresAt,
                true,
                now
        );
    }
}
