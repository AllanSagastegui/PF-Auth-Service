package pe.ask.auth.core.port.out;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface SecurityAuditOutputPort {
    Mono<Void> recordSecurityEvent(String eventType, UUID userId, String detail, Instant timestamp);
}
