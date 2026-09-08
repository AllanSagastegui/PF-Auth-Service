package pe.ask.auth.output.security.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public record RateLimiterCounter(AtomicInteger count, Instant windowExpiresAt) {
}
