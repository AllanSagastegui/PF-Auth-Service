package pe.ask.auth.output.database.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("refresh_tokens")
public final class RefreshTokenEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID sessionId;
    private UUID familyId;
    private UUID userId;
    private UUID deviceId;
    private String tokenHash;
    private String status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private Long version;

    @Transient
    private boolean isNewRecord;

    public RefreshTokenEntity() {
    }

    public RefreshTokenEntity(
            UUID id,
            UUID sessionId,
            UUID familyId,
            UUID userId,
            UUID deviceId,
            String tokenHash,
            String status,
            Instant createdAt,
            Instant expiresAt,
            Instant revokedAt,
            Long version,
            boolean isNewRecord
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.familyId = familyId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.version = version;
        this.isNewRecord = isNewRecord;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNewRecord;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setNewRecord(boolean newRecord) {
        isNewRecord = newRecord;
    }
}
