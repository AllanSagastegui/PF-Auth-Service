package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for confirming email verification")
public record EmailVerificationConfirmRequest(
        @Schema(description = "Verification token received via email", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Token is required")
        String token
) {
}
