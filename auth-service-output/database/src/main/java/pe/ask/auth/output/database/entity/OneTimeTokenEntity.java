package pe.ask.auth.output.database.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("one_time_tokens")
public final class OneTimeTokenEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private String type;
    private Instant createdAt;
    private Instant expiresAt;
    private Boolean used;
    private Instant usedAt;

    @Transient
    private boolean isNewRecord;

    public OneTimeTokenEntity() {
    }

    @SuppressWarnings("java:S107")
    public OneTimeTokenEntity(
            UUID id,
            UUID userId,
            String tokenHash,
            String type,
            Instant createdAt,
            Instant expiresAt,
            Boolean used,
            Instant usedAt,
            boolean isNewRecord
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.type = type;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = used;
        this.usedAt = usedAt;
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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public void setNewRecord(boolean newRecord) {
        isNewRecord = newRecord;
    }
}
