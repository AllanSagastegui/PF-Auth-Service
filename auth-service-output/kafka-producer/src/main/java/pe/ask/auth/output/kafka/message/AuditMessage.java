package pe.ask.auth.output.kafka.message;

import java.time.Instant;
import java.util.UUID;

public record AuditMessage(
        String eventType,
        UUID userId,
        String detail,
        Instant timestamp
) {
}
