package pe.ask.auth.input.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "Payload for verifying TOTP MFA challenge during login")
public record VerifyMfaRequest(
        @Schema(description = "Temporary MFA challenge token issued during initial login", example = "eyJhbGciOi...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "MFA challenge token is required")
        String mfaChallengeToken,

        @Schema(description = "6-digit TOTP code from authenticator app", example = "123456", pattern = "^\\d{6}$", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "TOTP code is required")
        @Pattern(regexp = "^\\d{6}$", message = "TOTP code must be 6 digits")
        String totpCode,

        @Schema(description = "Optional client device UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID deviceId
) {
}
