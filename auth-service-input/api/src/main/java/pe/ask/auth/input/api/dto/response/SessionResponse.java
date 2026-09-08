package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("java:S1313")
@Schema(description = "User session details")
public record SessionResponse(
        @Schema(description = "Unique session identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Device UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID deviceId,

        @Schema(description = "Client IP address", example = "192.168.1.100")
        String ipAddress,

        @Schema(description = "Client User-Agent header", example = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
        String userAgent,

        @Schema(description = "Session creation timestamp", example = "2026-03-30T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Last activity timestamp", example = "2026-03-30T10:05:00Z")
        Instant lastActivityAt,

        @Schema(description = "Session expiration timestamp", example = "2026-04-06T10:00:00Z")
        Instant expiresAt
) {
}
