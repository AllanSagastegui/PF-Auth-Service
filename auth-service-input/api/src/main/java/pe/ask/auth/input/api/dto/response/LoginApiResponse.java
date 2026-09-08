package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API response wrapper for authentication login")
public record LoginApiResponse(
        @Schema(description = "Indicates whether the request was successful", example = "true")
        Boolean success,

        @Schema(description = "Login result data payload")
        LoginResponse data,

        @Schema(description = "Error details payload if unsuccessful", nullable = true)
        ApiErrorResponse error,

        @Schema(description = "Timestamp when the response was generated", example = "2026-03-30T12:00:00Z")
        Instant timestamp
) {
}
