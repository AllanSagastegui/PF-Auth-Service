package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "List of active sessions response")
public record SessionsResponse(
        @Schema(description = "Active user sessions")
        List<SessionResponse> sessions
) {
}
