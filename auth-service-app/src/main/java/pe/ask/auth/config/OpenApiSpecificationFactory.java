package pe.ask.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import pe.ask.auth.config.model.OpenApiOperationEnum;

import java.util.List;

public final class OpenApiSpecificationFactory {

    private static final String FORMAT_DATE_TIME = "date-time";

    private OpenApiSpecificationFactory() {
    }

    public static void populateSpecification(OpenAPI openAPI, String bearerSchemeName) {
        Components components = openAPI.getComponents() != null ? openAPI.getComponents() : new Components();
        registerSchemas(components);
        openAPI.setComponents(components);

        Paths paths = new Paths();
        registerRoutes(paths, bearerSchemeName);
        openAPI.setPaths(paths);
    }

    private static void registerSchemas(Components components) {
        // Shared schemas
        components.addSchemas("ApiErrorResponse", new ObjectSchema()
                .description("Standard API error payload")
                .addProperty("code", new StringSchema().example("AUTH_INVALID_CREDENTIALS"))
                .addProperty("message", new StringSchema().example("Invalid email or password"))
                .addProperty("title", new StringSchema().example("Unauthorized"))
                .addProperty("status", new IntegerSchema().example(401))
                .addProperty("errors", new ObjectSchema().description("Field validation errors map, if applicable"))
        );

        // Requests
        components.addSchemas("RegisterRequest", new ObjectSchema()
                .description("User registration request")
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addProperty("password", new StringSchema().example("VerySecurePassword123!"))
                .addRequiredItem("email").addRequiredItem("password")
        );

        components.addSchemas("EmailVerificationRequest", new ObjectSchema()
                .description("Email verification dispatch request")
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addRequiredItem("email")
        );

        components.addSchemas("EmailVerificationConfirmRequest", new ObjectSchema()
                .description("Email verification confirmation token request")
                .addProperty("token", new StringSchema().example("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"))
                .addRequiredItem("token")
        );

        components.addSchemas("LoginRequest", new ObjectSchema()
                .description("User login request")
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addProperty("password", new StringSchema().example("VerySecurePassword123!"))
                .addProperty("deviceId", new UUIDSchema().example("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"))
                .addRequiredItem("email").addRequiredItem("password")
        );

        components.addSchemas("VerifyMfaRequest", new ObjectSchema()
                .description("MFA challenge verification request")
                .addProperty("mfaChallengeToken", new StringSchema().example("mfa_challenge_abc123"))
                .addProperty("totpCode", new StringSchema().example("123456").pattern("^\\d{6}$"))
                .addProperty("deviceId", new UUIDSchema().example("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"))
                .addRequiredItem("mfaChallengeToken").addRequiredItem("totpCode")
        );

        components.addSchemas("RefreshTokenRequest", new ObjectSchema()
                .description("Refresh token request")
                .addProperty("refreshToken", new StringSchema().example("rt_3fa85f6457174562b3fc2c963f66afa6"))
                .addProperty("deviceId", new UUIDSchema().example("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"))
                .addRequiredItem("refreshToken")
        );

        components.addSchemas("LogoutRequest", new ObjectSchema()
                .description("Logout session request")
                .addProperty("refreshToken", new StringSchema().example("rt_3fa85f6457174562b3fc2c963f66afa6"))
                .addProperty("deviceId", new UUIDSchema().example("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"))
                .addRequiredItem("refreshToken")
        );

        components.addSchemas("ForgotPasswordRequest", new ObjectSchema()
                .description("Forgot password email request")
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addRequiredItem("email")
        );

        components.addSchemas("ResetPasswordRequest", new ObjectSchema()
                .description("Reset password with token request")
                .addProperty("token", new StringSchema().example("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"))
                .addProperty("newPassword", new StringSchema().example("NewVerySecurePassword123!"))
                .addRequiredItem("token").addRequiredItem("newPassword")
        );

        components.addSchemas("ChangePasswordRequest", new ObjectSchema()
                .description("Change password request")
                .addProperty("currentPassword", new StringSchema().example("CurrentPassword123!"))
                .addProperty("newPassword", new StringSchema().example("NewVerySecurePassword123!"))
                .addRequiredItem("currentPassword").addRequiredItem("newPassword")
        );

        components.addSchemas("ConfirmMfaRequest", new ObjectSchema()
                .description("Confirm MFA enrollment request")
                .addProperty("totpCode", new StringSchema().example("654321").pattern("^\\d{6}$"))
                .addRequiredItem("totpCode")
        );

        components.addSchemas("DisableMfaRequest", new ObjectSchema()
                .description("Disable MFA request")
                .addProperty("password", new StringSchema().example("VerySecurePassword123!"))
                .addProperty("totpCode", new StringSchema().example("654321").pattern("^\\d{6}$"))
                .addRequiredItem("password").addRequiredItem("totpCode")
        );

        // Data Responses
        components.addSchemas("RegisterResponse", new ObjectSchema()
                .addProperty("userId", new UUIDSchema().example("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addProperty("message", new StringSchema().example("User registered successfully"))
        );

        components.addSchemas("TokenResponse", new ObjectSchema()
                .addProperty("accessToken", new StringSchema().example("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."))
                .addProperty("refreshToken", new StringSchema().example("rt_3fa85f6457174562b3fc2c963f66afa6"))
                .addProperty("tokenType", new StringSchema().example("Bearer"))
                .addProperty("expiresIn", new IntegerSchema().example(300))
        );

        components.addSchemas("LoginResponse", new ObjectSchema()
                .addProperty("mfaRequired", new BooleanSchema().example(false))
                .addProperty("mfaChallengeToken", new StringSchema().example("mfa_challenge_abc123"))
                .addProperty("tokens", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "TokenResponse"))
        );

        components.addSchemas("MessageResponse", new ObjectSchema()
                .addProperty("message", new StringSchema().example("Operation completed successfully"))
        );

        components.addSchemas("MeResponse", new ObjectSchema()
                .addProperty("id", new UUIDSchema().example("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .addProperty("email", new StringSchema().example("user@example.com"))
                .addProperty("status", new StringSchema().example("ACTIVE"))
                .addProperty("roles", new ArraySchema().items(new StringSchema().example("USER")))
                .addProperty("mfaEnabled", new BooleanSchema().example(true))
                .addProperty("createdAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-01-01T00:00:00Z"))
        );

        components.addSchemas("SessionResponse", new ObjectSchema()
                .addProperty("id", new UUIDSchema().example("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .addProperty("deviceId", new UUIDSchema().example("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"))
                .addProperty("ipAddress", new StringSchema().example("192.168.1.100"))
                .addProperty("userAgent", new StringSchema().example("Mozilla/5.0"))
                .addProperty("createdAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T10:00:00Z"))
                .addProperty("lastActivityAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T10:05:00Z"))
                .addProperty("expiresAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-04-06T10:00:00Z"))
        );

        components.addSchemas("SessionsResponse", new ObjectSchema()
                .addProperty("sessions", new ArraySchema().items(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "SessionResponse")))
        );

        components.addSchemas("EnrollMfaResponse", new ObjectSchema()
                .addProperty("secret", new StringSchema().example("JBSWY3DPEHPK3PXP"))
                .addProperty("qrCodeUri", new StringSchema().example("otpauth://totp/AskPlatform:user@example.com?secret=JBSWY3DPEHPK3PXP"))
                .addProperty("recoveryCodes", new ArraySchema().items(new StringSchema().example("a1b2-c3d4")))
        );

        components.addSchemas("JwksKeyResponse", new ObjectSchema()
                .addProperty("kty", new StringSchema().example("RSA"))
                .addProperty("crv", new StringSchema().example("P-256"))
                .addProperty("kid", new StringSchema().example("ask-auth-key-2026-01"))
                .addProperty("use", new StringSchema().example("sig"))
                .addProperty("alg", new StringSchema().example("RS256"))
                .addProperty("x", new StringSchema().example("u1WguNzsJwgIqV0EA3XGMsV9WU"))
                .addProperty("y", new StringSchema().example("AQAB"))
        );

        components.addSchemas("JwksResponse", new ObjectSchema()
                .addProperty("keys", new ArraySchema().items(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "JwksKeyResponse")))
        );

        // API Response Wrappers
        registerResponseWrapper(components, "RegisterApiResponse", "RegisterResponse");
        registerResponseWrapper(components, "LoginApiResponse", "LoginResponse");
        registerResponseWrapper(components, "TokenApiResponse", "TokenResponse");
        registerResponseWrapper(components, "MessageApiResponse", "MessageResponse");
        registerResponseWrapper(components, "MeApiResponse", "MeResponse");
        registerResponseWrapper(components, "SessionsApiResponse", "SessionsResponse");
        registerResponseWrapper(components, "EnrollMfaApiResponse", "EnrollMfaResponse");

        components.addSchemas("ErrorApiResponse", new ObjectSchema()
                .description("Standard API error response wrapper")
                .addProperty("success", new BooleanSchema().example(false))
                .addProperty("data", new ObjectSchema().nullable(true).example(null))
                .addProperty("error", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "ApiErrorResponse"))
                .addProperty("timestamp", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T12:00:00Z"))
        );
    }

    private static void registerResponseWrapper(Components components, String wrapperName, String dataSchemaName) {
        components.addSchemas(wrapperName, new ObjectSchema()
                .description("Standard API response wrapper for " + dataSchemaName)
                .addProperty("success", new BooleanSchema().example(true))
                .addProperty("data", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + dataSchemaName))
                .addProperty("error", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "ApiErrorResponse").nullable(true))
                .addProperty("timestamp", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T12:00:00Z"))
        );
    }

    private static void registerRoutes(Paths paths, String bearerScheme) {
        // 1. POST /api/v1/auth/register
        paths.addPathItem(OpenApiOperationEnum.PATH_REGISTER.value(), new PathItem().post(
                createOperation("register", "Register new user account",
                        "Registers a new user account with email and password, creates verification token and sends verification email event.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody("RegisterRequest", "Registration payload with email and password"))
                        .responses(createResponses("201", "RegisterApiResponse", List.of("400", "409", "429")))
        ));

        // 2. POST /api/v1/auth/email/verify/request
        paths.addPathItem(OpenApiOperationEnum.PATH_EMAIL_VERIFY_REQUEST.value(), new PathItem().post(
                createOperation("requestEmailVerification", "Request email verification token",
                        "Requests a new verification token to be dispatched to the user's email address.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody("EmailVerificationRequest", "Email verification request payload"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "429")))
        ));

        // 3. POST /api/v1/auth/email/verify/confirm
        paths.addPathItem(OpenApiOperationEnum.PATH_EMAIL_VERIFY_CONFIRM.value(), new PathItem().post(
                createOperation("confirmEmailVerification", "Confirm email verification",
                        "Validates the one-time token received via email and transitions user status to ACTIVE.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody("EmailVerificationConfirmRequest", "Confirmation payload containing verification token"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "404")))
        ));

        // 4. POST /api/v1/auth/login
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGIN.value(), new PathItem().post(
                createOperation("login", "Authenticate user credentials",
                        "Authenticates email and password. If MFA is active, returns an ephemeral challenge token; otherwise returns access/refresh token pair and creates a new session.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody("LoginRequest", "User credentials and optional device identifier"))
                        .responses(createResponses("200", "LoginApiResponse", List.of("400", "401", "403", "429")))
        ));

        // 5. POST /api/v1/auth/mfa/verify
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_VERIFY.value(), new PathItem().post(
                createOperation("verifyMfa", "Verify MFA challenge during login",
                        "Validates a 6-digit TOTP code against the temporary MFA challenge token issued at login.",
                        OpenApiOperationEnum.TAG_MFA.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody("VerifyMfaRequest", "MFA challenge token and TOTP verification code"))
                        .responses(createResponses("200", "TokenApiResponse", List.of("400", "401", "429")))
        ));

        // 6. POST /api/v1/auth/refresh
        paths.addPathItem(OpenApiOperationEnum.PATH_REFRESH.value(), new PathItem().post(
                createOperation("refreshToken", "Refresh access and refresh tokens",
                        "Performs refresh token rotation: revokes the old refresh token and issues a new access/refresh token pair.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody("RefreshTokenRequest", "Valid refresh token and optional device identifier"))
                        .responses(createResponses("200", "TokenApiResponse", List.of("400", "401")))
        ));

        // 7. POST /api/v1/auth/logout
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGOUT.value(), new PathItem().post(
                createOperation("logout", "Logout specific session",
                        "Revokes the session and marks the refresh token as revoked.",
                        OpenApiOperationEnum.TAG_AUTH.value(), bearerScheme)
                        .requestBody(createRequestBody("LogoutRequest", "Refresh token to invalidate"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "401")))
        ));

        // 8. POST /api/v1/auth/logout/all
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGOUT_ALL.value(), new PathItem().post(
                createOperation("logoutAll", "Logout all user sessions",
                        "Revokes all active sessions and refresh tokens belonging to the authenticated user.",
                        OpenApiOperationEnum.TAG_AUTH.value(), bearerScheme)
                        .responses(createResponses("200", "MessageApiResponse", List.of("401")))
        ));

        // 9. POST /api/v1/auth/password/forgot
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_FORGOT.value(), new PathItem().post(
                createOperation("forgotPassword", "Request password reset email",
                        "Generates a password reset token and sends an email notification if an account matches the email address.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), null)
                        .requestBody(createRequestBody("ForgotPasswordRequest", "Target account email address"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "429")))
        ));

        // 10. POST /api/v1/auth/password/reset
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_RESET.value(), new PathItem().post(
                createOperation("resetPassword", "Reset password with token",
                        "Resets user password using the verification token and invalidates all existing sessions.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), null)
                        .requestBody(createRequestBody("ResetPasswordRequest", "Reset token and new password"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400")))
        ));

        // 11. POST /api/v1/auth/password/change
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_CHANGE.value(), new PathItem().post(
                createOperation("changePassword", "Change password for authenticated user",
                        "Updates password for the currently logged-in user after verifying current credentials.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), bearerScheme)
                        .requestBody(createRequestBody("ChangePasswordRequest", "Current and new password payload"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "401")))
        ));

        // 12. GET /api/v1/auth/me
        paths.addPathItem(OpenApiOperationEnum.PATH_ME.value(), new PathItem().get(
                createOperation("getMe", "Get authenticated user profile",
                        "Retrieves the authenticated user's profile details including roles, status, and MFA status.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .responses(createResponses("200", "MeApiResponse", List.of("401")))
        ));

        // 13. GET /api/v1/auth/sessions
        paths.addPathItem(OpenApiOperationEnum.PATH_SESSIONS.value(), new PathItem().get(
                createOperation("getSessions", "List active user sessions",
                        "Returns a list of all active sessions for the authenticated user, including IP address, user agent, and activity timestamps.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .responses(createResponses("200", "SessionsApiResponse", List.of("401")))
        ));

        // 14. DELETE /api/v1/auth/sessions/{id}
        paths.addPathItem(OpenApiOperationEnum.PATH_SESSION_BY_ID.value(), new PathItem().delete(
                createOperation("revokeSession", "Revoke specific user session",
                        "Revokes a single active session identified by its session UUID.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .addParametersItem(new PathParameter().name(OpenApiOperationEnum.PARAM_SESSION_ID.value()).description(OpenApiOperationEnum.PARAM_SESSION_ID_DESC.value()).required(true).schema(new UUIDSchema()))
                        .responses(createResponses("200", "MessageApiResponse", List.of("401", "404")))
        ));

        // 15. POST /api/v1/auth/mfa/enroll
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_ENROLL.value(), new PathItem().post(
                createOperation("enrollMfa", "Enroll in TOTP MFA",
                        "Generates a new TOTP secret and otpauth URI with recovery codes for QR code scanning.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .responses(createResponses("200", "EnrollMfaApiResponse", List.of("401", "409")))
        ));

        // 16. POST /api/v1/auth/mfa/confirm
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_CONFIRM.value(), new PathItem().post(
                createOperation("confirmMfa", "Confirm and activate TOTP MFA",
                        "Validates the initial 6-digit TOTP code and marks MFA as activated for the user.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .requestBody(createRequestBody("ConfirmMfaRequest", "TOTP confirmation code"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "401")))
        ));

        // 17. DELETE /api/v1/auth/mfa/disable
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_DISABLE.value(), new PathItem().delete(
                createOperation("disableMfa", "Disable TOTP MFA",
                        "Disables MFA on user account after verifying current password and a valid 6-digit TOTP code.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .requestBody(createRequestBody("DisableMfaRequest", "Password and TOTP code for MFA disable confirmation"))
                        .responses(createResponses("200", "MessageApiResponse", List.of("400", "401")))
        ));

        // 18. GET /.well-known/jwks.json
        paths.addPathItem(OpenApiOperationEnum.PATH_JWKS.value(), new PathItem().get(
                createOperation("getJwks", "Get JSON Web Key Set (JWKS)",
                        "Returns public keys for validating JWT tokens issued by this authentication service.",
                        OpenApiOperationEnum.TAG_JWKS.value(), null)
                        .responses(createSuccessOnlyResponses("200", "JwksResponse"))
        ));
    }

    private static Operation createOperation(String opId, String summary, String description, String tag, String bearerScheme) {
        Operation operation = new Operation()
                .operationId(opId)
                .summary(summary)
                .description(description)
                .addTagsItem(tag)
                .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_CORRELATION_ID.value()).description(OpenApiOperationEnum.HEADER_CORRELATION_ID_DESC.value()).required(false))
                .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_REQUEST_ID.value()).description(OpenApiOperationEnum.HEADER_REQUEST_ID_DESC.value()).required(false));

        if (bearerScheme != null) {
            operation.addSecurityItem(new SecurityRequirement().addList(bearerScheme));
        }
        return operation;
    }

    private static RequestBody createRequestBody(String schemaName, String description) {
        return new RequestBody()
                .description(description)
                .required(true)
                .content(new Content().addMediaType(OpenApiOperationEnum.CONTENT_TYPE_JSON.value(),
                        new MediaType().schema(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + schemaName))));
    }

    private static ApiResponses createResponses(String successCode, String successSchemaName, List<String> errorCodes) {
        ApiResponses responses = new ApiResponses();
        String successDesc = "201".equals(successCode) ? OpenApiOperationEnum.STATUS_201_DESC.value() : OpenApiOperationEnum.STATUS_200_DESC.value();
        responses.addApiResponse(successCode, new ApiResponse()
                .description(successDesc)
                .content(new Content().addMediaType(OpenApiOperationEnum.CONTENT_TYPE_JSON.value(),
                        new MediaType().schema(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + successSchemaName)))));

        for (String errCode : errorCodes) {
            String desc = switch (errCode) {
                case "400" -> OpenApiOperationEnum.STATUS_400_DESC.value();
                case "401" -> OpenApiOperationEnum.STATUS_401_DESC.value();
                case "403" -> OpenApiOperationEnum.STATUS_403_DESC.value();
                case "404" -> OpenApiOperationEnum.STATUS_404_DESC.value();
                case "409" -> OpenApiOperationEnum.STATUS_409_DESC.value();
                case "429" -> OpenApiOperationEnum.STATUS_429_DESC.value();
                default -> "Error response";
            };
            responses.addApiResponse(errCode, new ApiResponse()
                    .description(desc)
                    .content(new Content().addMediaType(OpenApiOperationEnum.CONTENT_TYPE_JSON.value(),
                            new MediaType().schema(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + "ErrorApiResponse")))));
        }
        return responses;
    }

    private static ApiResponses createSuccessOnlyResponses(String successCode, String successSchemaName) {
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse(successCode, new ApiResponse()
                .description(OpenApiOperationEnum.STATUS_200_DESC.value())
                .content(new Content().addMediaType(OpenApiOperationEnum.CONTENT_TYPE_JSON.value(),
                        new MediaType().schema(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + successSchemaName)))));
        return responses;
    }
}
