package pe.ask.auth.input.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard generic API response wrapper")
public record ApiResponse<T>(
        @Schema(description = "Indicates whether the request was processed successfully", example = "true")
        Boolean success,

        @Schema(description = "Response data payload (null in case of error)")
        T data,

        @Schema(description = "Error response details (null in case of success)")
        ApiErrorResponse error,

        @Schema(description = "Timestamp when the response was generated", example = "2026-03-30T12:00:00Z")
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(ApiErrorResponse error) {
        return new ApiResponse<>(false, null, error, Instant.now());
    }
}
