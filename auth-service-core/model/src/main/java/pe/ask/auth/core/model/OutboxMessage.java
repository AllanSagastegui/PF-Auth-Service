package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

import java.time.Instant;
import java.util.UUID;

public record OutboxMessage(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        Instant createdAt,
        Instant nextAttemptAt,
        Instant processedAt,
        int retryCount
) {

    public OutboxMessage {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(aggregateType, "aggregateType");
        DomainValidation.requireNonNull(aggregateId, "aggregateId");
        DomainValidation.requireNonNull(eventType, "eventType");
        DomainValidation.requireNonNull(payload, "payload");
        DomainValidation.requireNonNull(createdAt, "createdAt");
    }

    public OutboxMessage markProcessed(Instant now) {
        return new OutboxMessage(
                id,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                createdAt,
                nextAttemptAt,
                now,
                retryCount
        );
    }

    public OutboxMessage incrementRetry(Instant nextAttempt) {
        return new OutboxMessage(
                id,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                createdAt,
                nextAttempt,
                processedAt,
                retryCount + 1
        );
    }
}
