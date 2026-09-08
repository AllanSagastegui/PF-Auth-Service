package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication login result")
public record LoginResponse(
        @Schema(description = "Whether MFA verification is required before issuing tokens", example = "false")
        boolean mfaRequired,

        @Schema(description = "Temporary challenge token if MFA is required", example = "mfa_challenge_token_abc123")
        String mfaChallengeToken,

        @Schema(description = "Authentication tokens (populated only when mfaRequired is false)")
        TokenResponse tokens
) {
    public static LoginResponse mfa(String challengeToken) {
        return new LoginResponse(true, challengeToken, null);
    }

    public static LoginResponse success(TokenResponse tokens) {
        return new LoginResponse(false, null, tokens);
    }
}
