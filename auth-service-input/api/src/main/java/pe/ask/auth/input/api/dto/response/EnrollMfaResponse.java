package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "MFA enrollment details response")
public record EnrollMfaResponse(
        @Schema(description = "Base32-encoded TOTP secret key", example = "JBSWY3DPEHPK3PXP")
        String secret,

        @Schema(description = "otpauth:// URI for authenticator QR code rendering", example = "otpauth://totp/AskPlatform:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=AskPlatform")
        String qrCodeUri,

        @Schema(description = "List of backup recovery codes", example = "[\"a1b2-c3d4\", \"e5f6-g7h8\"]")
        List<String> recoveryCodes
) {
}
