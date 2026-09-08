package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for changing password of authenticated user")
public record ChangePasswordRequest(
        @Schema(description = "Current user password", example = "CurrentSecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password (minimum 15 characters, maximum 128 characters)", example = "BrandNewSecurePassword123!", minLength = 15, maxLength = 128, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(min = 15, max = 128, message = "Password must be between 15 and 128 characters")
        String newPassword
) {
}
