package pe.ask.auth.input.api.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import pe.ask.auth.input.api.error.ApiUnauthorizedException;
import pe.ask.auth.input.api.error.ApiValidationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SecurityRequestExtractor tests")
class SecurityRequestExtractorTest {

    @Test
    @DisplayName("extractUserId should extract valid UUID from Bearer JWT")
    void shouldExtractFromJwt() {
        UUID expectedId = UUID.randomUUID();
        String payloadJson = "{\"sub\":\"" + expectedId + "\",\"roles\":[\"ROLE_USER\"]}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String jwt = "eyJhbGciOiJSUzI1NiJ9." + encodedPayload + ".signaturesignature";

        ServerRequest request = MockServerRequest.builder()
                .header(ApiHeaderEnum.AUTHORIZATION.value(), "Bearer " + jwt)
                .build();

        UUID extracted = SecurityRequestExtractor.extractUserId(request);
        assertThat(extracted).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("extractUserId should fallback to X-User-Id when Bearer JWT is invalid")
    void shouldFallbackToHeaderOnMalformedJwt() {
        UUID expectedId = UUID.randomUUID();
        ServerRequest request = MockServerRequest.builder()
                .header(ApiHeaderEnum.AUTHORIZATION.value(), "Bearer invalid-jwt")
                .header(ApiHeaderEnum.USER_ID.value(), expectedId.toString())
                .build();

        UUID extracted = SecurityRequestExtractor.extractUserId(request);
        assertThat(extracted).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("extractUserId should extract directly from X-User-Id when no Authorization header")
    void shouldExtractFromUserIdHeader() {
        UUID expectedId = UUID.randomUUID();
        ServerRequest request = MockServerRequest.builder()
                .header(ApiHeaderEnum.USER_ID.value(), " " + expectedId + " ")
                .build();

        UUID extracted = SecurityRequestExtractor.extractUserId(request);
        assertThat(extracted).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("extractUserId should throw ApiUnauthorizedException on invalid X-User-Id UUID")
    void shouldThrowOnInvalidUserIdHeader() {
        ServerRequest request = MockServerRequest.builder()
                .header(ApiHeaderEnum.USER_ID.value(), "not-a-uuid")
                .build();

        assertThrows(ApiUnauthorizedException.class, () -> SecurityRequestExtractor.extractUserId(request));
    }

    @Test
    @DisplayName("extractUserId should throw ApiUnauthorizedException when both headers missing")
    void shouldThrowWhenNoHeaders() {
        ServerRequest request = MockServerRequest.builder().build();

        assertThrows(ApiUnauthorizedException.class, () -> SecurityRequestExtractor.extractUserId(request));
    }

    @Test
    @DisplayName("extractUserId should throw ApiValidationException when request is null")
    void shouldThrowWhenRequestNull() {
        assertThrows(ApiValidationException.class, () -> SecurityRequestExtractor.extractUserId(null));
    }
}
