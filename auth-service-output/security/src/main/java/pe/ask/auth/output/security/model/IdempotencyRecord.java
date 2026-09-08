package pe.ask.auth.output.security.model;

import java.time.Instant;

public record IdempotencyRecord(Object value, Instant expiresAt) {
}
