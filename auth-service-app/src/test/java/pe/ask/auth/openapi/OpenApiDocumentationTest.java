package pe.ask.auth.openapi;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.SpringDocUtils;
import pe.ask.auth.config.OpenApiConfiguration;
import pe.ask.auth.config.properties.AuthOpenApiProperties;
import pe.ask.auth.input.api.filter.ApiPathEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OpenApiDocumentationTest {

    private static final List<String> EXPECTED_PATHS = List.of(
            ApiPathEnum.REGISTER.value(),
            ApiPathEnum.EMAIL_VERIFY_REQUEST.value(),
            ApiPathEnum.EMAIL_VERIFY_CONFIRM.value(),
            ApiPathEnum.LOGIN.value(),
            ApiPathEnum.VERIFY_MFA.value(),
            ApiPathEnum.REFRESH.value(),
            ApiPathEnum.LOGOUT.value(),
            ApiPathEnum.LOGOUT_ALL.value(),
            ApiPathEnum.FORGOT_PASSWORD.value(),
            ApiPathEnum.RESET_PASSWORD.value(),
            ApiPathEnum.CHANGE_PASSWORD.value(),
            ApiPathEnum.ME.value(),
            ApiPathEnum.SESSIONS.value(),
            ApiPathEnum.SESSION_BY_ID.value(),
            ApiPathEnum.MFA_ENROLL.value(),
            ApiPathEnum.MFA_CONFIRM.value(),
            ApiPathEnum.MFA_DISABLE.value(),
            ApiPathEnum.JWKS.value()
    );

    @Test
    @DisplayName("OpenAPI specification must discover and document all 18 functional endpoints in high detail")
    void openApiMustContainAll18EndpointsDetailed() {
        OpenApiConfiguration config = new OpenApiConfiguration();
        AuthOpenApiProperties properties = new AuthOpenApiProperties(
                null, null, null, null, null, null, null, null, null, null, null
        );

        OpenAPI openAPI = config.customOpenAPI(properties);

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getPaths())
                .as("OpenAPI paths must not be null or empty")
                .isNotNull()
                .isNotEmpty();

        assertThat(openAPI.getPaths().keySet())
                .as("OpenAPI paths must contain all 18 functional routes matching ApiPathEnum")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_PATHS);

        // Verify component schemas are registered
        assertThat(openAPI.getComponents().getSchemas())
                .as("OpenAPI components schemas must contain all request, response and wrapper DTO definitions")
                .containsKeys(
                        "RegisterRequest",
                        "EmailVerificationRequest",
                        "EmailVerificationConfirmRequest",
                        "LoginRequest",
                        "VerifyMfaRequest",
                        "RefreshTokenRequest",
                        "LogoutRequest",
                        "ForgotPasswordRequest",
                        "ResetPasswordRequest",
                        "ChangePasswordRequest",
                        "ConfirmMfaRequest",
                        "DisableMfaRequest",
                        "RegisterResponse",
                        "LoginResponse",
                        "TokenResponse",
                        "MessageResponse",
                        "MeResponse",
                        "SessionResponse",
                        "SessionsResponse",
                        "EnrollMfaResponse",
                        "JwksResponse",
                        "JwksKeyResponse",
                        "ApiErrorResponse",
                        "RegisterApiResponse",
                        "LoginApiResponse",
                        "TokenApiResponse",
                        "MessageApiResponse",
                        "MeApiResponse",
                        "SessionsApiResponse",
                        "EnrollMfaApiResponse",
                        "ErrorApiResponse"
                );

        // Verify each route operation details
        openAPI.getPaths().forEach((path, pathItem) -> {
            List<Operation> operations = pathItem.readOperations();
            assertThat(operations)
                    .as("Route %s must have at least one HTTP operation", path)
                    .isNotEmpty();

            for (Operation op : operations) {
                assertThat(op.getOperationId()).as("OperationId for %s", path).isNotBlank();
                assertThat(op.getSummary()).as("Summary for %s", path).isNotBlank();
                assertThat(op.getDescription()).as("Description for %s", path).isNotBlank();
                assertThat(op.getTags()).as("Tags for %s", path).isNotEmpty();
                assertThat(op.getResponses()).as("Responses for %s", path).isNotEmpty();

                // Success response check
                boolean hasSuccess = op.getResponses().containsKey("200") || op.getResponses().containsKey("201");
                assertThat(hasSuccess)
                        .as("Route %s must define a 200 or 201 success response", path)
                        .isTrue();

                // Common tracing headers check
                boolean hasCorrelationHeader = op.getParameters() != null && op.getParameters().stream()
                        .anyMatch(p -> "X-Correlation-Id".equals(p.getName()));
                assertThat(hasCorrelationHeader)
                        .as("Route %s must document X-Correlation-Id header", path)
                        .isTrue();
            }
        });

        // Verify OpenAPI serialization to JSON and YAML without date/time or Jackson serialization errors
        String jsonOutput = assertDoesNotThrow(() -> Json.pretty(openAPI),
                "OpenAPI JSON serialization must not throw JsonProcessingException");
        assertThat(jsonOutput).isNotBlank().contains("/api/v1/auth/login");

        String yamlOutput = assertDoesNotThrow(() -> Yaml.pretty(openAPI),
                "OpenAPI YAML serialization must not throw JsonProcessingException");
        assertThat(yamlOutput).isNotBlank().contains("/api/v1/auth/login");

        // Verify SpringDoc cloneViaJson executes cleanly without JsonProcessingException or IllegalArgumentException
        OpenAPI cloned = assertDoesNotThrow(() -> SpringDocUtils.cloneViaJson(openAPI, new TypeReference<OpenAPI>() {}, Json.mapper()),
                "SpringDoc cloneViaJson must not throw exception on OpenAPI model");
        assertThat(cloned).isNotNull();
        assertThat(cloned.getPaths()).containsKey("/api/v1/auth/login");
    }
}
