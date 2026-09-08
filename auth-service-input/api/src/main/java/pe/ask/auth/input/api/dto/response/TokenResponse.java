package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Issued token pair response")
public record TokenResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Opaque refresh token", example = "rt_3fa85f6457174562b3fc2c963f66afa6")
        String refreshToken,

        @Schema(description = "Token type scheme", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token time-to-live in seconds", example = "300")
        long expiresIn
) {
}
