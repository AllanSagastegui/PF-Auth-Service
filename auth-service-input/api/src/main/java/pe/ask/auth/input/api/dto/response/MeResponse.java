package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Authenticated user profile details")
public record MeResponse(
        @Schema(description = "User unique identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "User email address", example = "user@example.com")
        String email,

        @Schema(description = "Account status", example = "ACTIVE")
        String status,

        @Schema(description = "Assigned user roles", example = "[\"USER\"]")
        Set<String> roles,

        @Schema(description = "Whether MFA is enabled on the account", example = "true")
        boolean mfaEnabled,

        @Schema(description = "Account creation timestamp", example = "2026-01-01T00:00:00Z")
        Instant createdAt
) {
}
