package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API response wrapper for error responses")
public record ErrorApiResponse(
        @Schema(description = "Indicates whether the request was successful", example = "false")
        Boolean success,

        @Schema(description = "Data payload (always null on error)", nullable = true, example = "null")
        Object data,

        @Schema(description = "Error details payload")
        ApiErrorResponse error,

        @Schema(description = "Timestamp when the response was generated", example = "2026-03-30T12:00:00Z")
        Instant timestamp
) {
}
