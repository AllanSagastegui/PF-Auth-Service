package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for resetting password with a reset token")
public record ResetPasswordRequest(
        @Schema(description = "Password reset token received via email", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Token is required")
        String token,

        @Schema(description = "New password (minimum 15 characters, maximum 128 characters)", example = "NewVerySecurePassword123!", minLength = 15, maxLength = 128, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(min = 15, max = 128, message = "Password must be between 15 and 128 characters")
        String newPassword
) {
}
