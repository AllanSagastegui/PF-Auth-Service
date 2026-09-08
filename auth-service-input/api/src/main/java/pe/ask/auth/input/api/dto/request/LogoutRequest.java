package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Payload for revoking a session and refresh token")
public record LogoutRequest(
        @Schema(description = "Refresh token to revoke", example = "rt_3fa85f6457174562b3fc2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required")
        String refreshToken,

        @Schema(description = "Optional client device UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID deviceId
) {
}
