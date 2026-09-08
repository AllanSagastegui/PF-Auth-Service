package pe.ask.auth.input.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.input.api.dto.response.ApiErrorResponse;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.dto.response.JwksKeyResponse;
import pe.ask.auth.input.api.dto.response.MeResponse;
import pe.ask.auth.input.api.dto.response.SessionResponse;
import pe.ask.auth.input.api.error.ApiUnauthorizedException;
import pe.ask.auth.input.api.error.ApiValidationException;
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiPathEnum;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API Errors and DTOs tests")
class ApiErrorsAndDtosTest {

    @Test
    @DisplayName("ApiValidationException accessors")
    void testValidationException() {
        Map<String, String> errors = Map.of("field", "must not be blank");
        ApiValidationException ex = new ApiValidationException(errors);
        assertThat(ex.getErrorCode()).isEqualTo("AUTH_VALIDATION_ERROR");
        assertThat(ex.getTitle()).isEqualTo("Validation Failed");
        assertThat(ex.getHttpStatus()).isEqualTo(400);
        assertThat(ex.getErrors()).containsEntry("field", "must not be blank");

        ApiValidationException nullEx = new ApiValidationException(null);
        assertThat(nullEx.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("ApiUnauthorizedException accessors")
    void testUnauthorizedException() {
        ApiUnauthorizedException ex = new ApiUnauthorizedException("Custom msg");
        assertThat(ex.getMessage()).isEqualTo("Custom msg");
        assertThat(ex.getErrorCode()).isEqualTo("AUTH_UNAUTHORIZED");
        assertThat(ex.getTitle()).isEqualTo("Unauthorized");
        assertThat(ex.getHttpStatus()).isEqualTo(401);

        ApiUnauthorizedException defaultEx = new ApiUnauthorizedException();
        assertThat(defaultEx.getMessage()).isEqualTo("Authentication required");

        ApiUnauthorizedException nullMsgEx = new ApiUnauthorizedException(null);
        assertThat(nullMsgEx.getMessage()).isEqualTo("Authentication required");
    }

    @Test
    @DisplayName("DTO accessors")
    void testDtoAccessors() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        SessionResponse session = new SessionResponse(id, UUID.randomUUID(), "1.1.1.1", "Agent", now, now, now);
        assertThat(session.id()).isEqualTo(id);
        assertThat(session.ipAddress()).isEqualTo("1.1.1.1");

        JwksKeyResponse key = new JwksKeyResponse("RSA", "P-256", "key-id", "sig", "RS256", "n", "e");
        assertThat(key.kty()).isEqualTo("RSA");
        assertThat(key.kid()).isEqualTo("key-id");

        MeResponse me = new MeResponse(id, "user@test.com", "ACTIVE", Set.of("ROLE_USER"), false, now);
        assertThat(me.id()).isEqualTo(id);
        assertThat(me.mfaEnabled()).isFalse();

        ApiErrorResponse err = new ApiErrorResponse("ERR_CODE", "detail", "Error title", 400, Map.of("k", "v"));
        assertThat(err.status()).isEqualTo(400);
        assertThat(err.code()).isEqualTo("ERR_CODE");
        assertThat(err.title()).isEqualTo("Error title");

        ApiErrorResponse shortErr = new ApiErrorResponse("ERR_CODE", "detail", "Error title", 400);
        assertThat(shortErr.status()).isEqualTo(400);

        ApiResponse<String> apiResp = ApiResponse.error(err);
        assertThat(apiResp.success()).isFalse();
        assertThat(apiResp.error()).isNotNull();
    }

    @Test
    @DisplayName("Enums coverage")
    void testEnumsCoverage() {
        for (ApiHeaderEnum header : ApiHeaderEnum.values()) {
            assertThat(header.value()).isNotBlank();
        }
        for (ApiMessageEnum msg : ApiMessageEnum.values()) {
            assertThat(msg.value()).isNotBlank();
        }
        for (ApiPathEnum path : ApiPathEnum.values()) {
            assertThat(path.value()).isNotBlank();
        }
    }
}
