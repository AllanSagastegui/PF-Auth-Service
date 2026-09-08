package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public record User(
        UUID id,
        String rawEmail,
        String canonicalEmail,
        String passwordHash,
        UserStatus status,
        Set<Role> roles,
        long authVersion,
        boolean mfaEnabled,
        String totpSecretEncrypted,
        Instant createdAt,
        Instant updatedAt
) {

    public User {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(rawEmail, "rawEmail");
        DomainValidation.requireNonNull(canonicalEmail, "canonicalEmail");
        DomainValidation.requireNonNull(passwordHash, "passwordHash");
        DomainValidation.requireNonNull(status, "status");
        roles = roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
    }

    public static String canonicalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public boolean canAuthenticate() {
        return status == UserStatus.ACTIVE;
    }

    public User verifyEmail(Instant at) {
        if (this.status == UserStatus.PENDING_VERIFICATION) {
            return new User(
                    id,
                    rawEmail,
                    canonicalEmail,
                    passwordHash,
                    UserStatus.ACTIVE,
                    roles,
                    authVersion,
                    mfaEnabled,
                    totpSecretEncrypted,
                    createdAt,
                    at
            );
        }
        return this;
    }

    public User changePassword(String newPasswordHash, Instant at) {
        DomainValidation.requireNonNull(newPasswordHash, "newPasswordHash");
        return new User(
                id,
                rawEmail,
                canonicalEmail,
                newPasswordHash,
                status,
                roles,
                authVersion + 1,
                mfaEnabled,
                totpSecretEncrypted,
                createdAt,
                at
        );
    }

    public User incrementAuthVersion(Instant at) {
        return new User(
                id,
                rawEmail,
                canonicalEmail,
                passwordHash,
                status,
                roles,
                authVersion + 1,
                mfaEnabled,
                totpSecretEncrypted,
                createdAt,
                at
        );
    }

    public User enableMfa(String encryptedSecret, Instant at) {
        DomainValidation.requireNonNull(encryptedSecret, "encryptedSecret");
        return new User(
                id,
                rawEmail,
                canonicalEmail,
                passwordHash,
                status,
                roles,
                authVersion + 1,
                true,
                encryptedSecret,
                createdAt,
                at
        );
    }

    public User disableMfa(Instant at) {
        return new User(
                id,
                rawEmail,
                canonicalEmail,
                passwordHash,
                status,
                roles,
                authVersion + 1,
                false,
                null,
                createdAt,
                at
        );
    }
}
