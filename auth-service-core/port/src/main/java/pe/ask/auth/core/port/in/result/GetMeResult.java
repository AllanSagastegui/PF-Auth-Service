package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record GetMeResult(
        UUID id,
        String email,
        String status,
        Set<String> roles,
        boolean mfaEnabled,
        Instant createdAt
) {
    public GetMeResult {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(email, "email");
        DomainValidation.requireNonNull(status, "status");
        roles = roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
    }
}
