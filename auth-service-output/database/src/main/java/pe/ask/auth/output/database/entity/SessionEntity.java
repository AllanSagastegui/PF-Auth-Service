package pe.ask.auth.output.database.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("sessions")
public final class SessionEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID userId;
    private UUID deviceId;
    private String status;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant lastActivityAt;
    private Instant expiresAt;

    @Transient
    private boolean isNewRecord;

    public SessionEntity() {
    }

    @SuppressWarnings("java:S107")
    public SessionEntity(
            UUID id,
            UUID userId,
            UUID deviceId,
            String status,
            String ipAddress,
            String userAgent,
            Instant createdAt,
            Instant lastActivityAt,
            Instant expiresAt,
            boolean isNewRecord
    ) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.status = status;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
        this.expiresAt = expiresAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setNewRecord(boolean newRecord) {
        isNewRecord = newRecord;
    }
}
