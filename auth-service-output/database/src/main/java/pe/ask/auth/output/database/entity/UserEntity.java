package pe.ask.auth.output.database.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import pe.ask.persistence.core.entity.Entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Table("users")
public final class UserEntity extends Entity implements Persistable<UUID> {

    @Id
    private UUID id;
    private String rawEmail;
    private String canonicalEmail;
    private String passwordHash;
    private String status;
    private String roles;
    private Long authVersion;
    private Boolean mfaEnabled;
    private String totpSecretEncrypted;

    public UserEntity() {
    }

    public UserEntity(
            UUID id,
            String rawEmail,
            String canonicalEmail,
            String passwordHash,
            String status,
            String roles,
            Long authVersion,
            Boolean mfaEnabled,
            String totpSecretEncrypted,
            Instant createdAt,
            Instant updatedAt,
            boolean isNewRecord
    ) {
        this.id = id;
        this.rawEmail = rawEmail;
        this.canonicalEmail = canonicalEmail;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles = roles;
        this.authVersion = authVersion;
        this.mfaEnabled = mfaEnabled;
        this.totpSecretEncrypted = totpSecretEncrypted;
        if (createdAt != null) {
            setCreatedAt(LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC));
        }
        if (updatedAt != null) {
            setUpdatedAt(LocalDateTime.ofInstant(updatedAt, ZoneOffset.UTC));
        }
        setNew(isNewRecord);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRawEmail() {
        return rawEmail;
    }

    public void setRawEmail(String rawEmail) {
        this.rawEmail = rawEmail;
    }

    public String getCanonicalEmail() {
        return canonicalEmail;
    }

    public void setCanonicalEmail(String canonicalEmail) {
        this.canonicalEmail = canonicalEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Long getAuthVersion() {
        return authVersion;
    }

    public void setAuthVersion(Long authVersion) {
        this.authVersion = authVersion;
    }

    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getTotpSecretEncrypted() {
        return totpSecretEncrypted;
    }

    public void setTotpSecretEncrypted(String totpSecretEncrypted) {
        this.totpSecretEncrypted = totpSecretEncrypted;
    }

    public Instant getCreatedAtInstant() {
        return getCreatedAt() != null ? getCreatedAt().toInstant(ZoneOffset.UTC) : null;
    }

    public void setCreatedAtInstant(Instant instant) {
        if (instant != null) {
            setCreatedAt(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
        } else {
            setCreatedAt(null);
        }
    }

    public Instant getUpdatedAtInstant() {
        return getUpdatedAt() != null ? getUpdatedAt().toInstant(ZoneOffset.UTC) : null;
    }

    public void setUpdatedAtInstant(Instant instant) {
        if (instant != null) {
            setUpdatedAt(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
        } else {
            setUpdatedAt(null);
        }
    }

    public void setNewRecord(boolean newRecord) {
        setNew(newRecord);
    }
}
