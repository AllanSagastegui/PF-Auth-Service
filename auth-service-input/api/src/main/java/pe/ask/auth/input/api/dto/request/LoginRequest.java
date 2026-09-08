package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Payload for user authentication")
public record LoginRequest(
        @Schema(description = "User email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "VerySecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "Optional client device UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID deviceId
) {
}
