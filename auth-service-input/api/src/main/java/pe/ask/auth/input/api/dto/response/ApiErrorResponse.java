package pe.ask.auth.input.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error payload")
public record ApiErrorResponse(
        @Schema(description = "Domain-specific error code", example = "AUTH_INVALID_CREDENTIALS")
        String code,

        @Schema(description = "Human-readable error explanation", example = "Invalid email or password")
        String message,

        @Schema(description = "HTTP status title", example = "Unauthorized")
        String title,

        @Schema(description = "HTTP status code", example = "401")
        Integer status,

        @Schema(description = "Field validation errors map, if applicable")
        Map<String, String> errors
) {
    public ApiErrorResponse(String code, String message, String title, Integer status) {
        this(code, message, title, status, null);
    }
}
