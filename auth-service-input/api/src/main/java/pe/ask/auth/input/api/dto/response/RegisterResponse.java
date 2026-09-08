package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "User registration result")
public record RegisterResponse(
        @Schema(description = "Assigned user identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,

        @Schema(description = "Registered email address", example = "user@example.com")
        String email,

        @Schema(description = "Informational status message", example = "User registered successfully")
        String message
) {
}
