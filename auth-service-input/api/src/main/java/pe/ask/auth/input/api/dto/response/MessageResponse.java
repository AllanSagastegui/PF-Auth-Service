package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informational status response")
public record MessageResponse(
        @Schema(description = "Response message text", example = "Operation completed successfully")
        String message
) {
}
