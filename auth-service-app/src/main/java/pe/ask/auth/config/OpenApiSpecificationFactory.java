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

@SuppressWarnings({"java:S1192", "java:S1313", "java:S6418"})
public final class OpenApiSpecificationFactory {

    private static final String FORMAT_DATE_TIME = "date-time";
    private static final String SCHEMA_API_ERROR_RESPONSE = "ApiErrorResponse";
    private static final String SCHEMA_TOKEN_RESPONSE = "TokenResponse";
    private static final String SCHEMA_TOKEN_API_RESPONSE = "TokenApiResponse";
    private static final String SCHEMA_MESSAGE_API_RESPONSE = "MessageApiResponse";
    private static final String SCHEMA_REGISTER_REQUEST = "RegisterRequest";
    private static final String SCHEMA_EMAIL_VERIFY_REQUEST = "EmailVerificationRequest";
    private static final String SCHEMA_EMAIL_VERIFY_CONFIRM_REQUEST = "EmailVerificationConfirmRequest";
    private static final String SCHEMA_LOGIN_REQUEST = "LoginRequest";
    private static final String SCHEMA_VERIFY_MFA_REQUEST = "VerifyMfaRequest";
    private static final String SCHEMA_REFRESH_TOKEN_REQUEST = "RefreshTokenRequest";
    private static final String SCHEMA_LOGOUT_REQUEST = "LogoutRequest";
    private static final String SCHEMA_FORGOT_PASSWORD_REQUEST = "ForgotPasswordRequest";
    private static final String SCHEMA_RESET_PASSWORD_REQUEST = "ResetPasswordRequest";
    private static final String SCHEMA_CHANGE_PASSWORD_REQUEST = "ChangePasswordRequest";
    private static final String SCHEMA_CONFIRM_MFA_REQUEST = "ConfirmMfaRequest";
    private static final String SCHEMA_DISABLE_MFA_REQUEST = "DisableMfaRequest";
    private static final String SCHEMA_SESSION_RESPONSE = "SessionResponse";

    private static final String PROP_MESSAGE = "message";
    private static final String PROP_EMAIL = "email";
    private static final String PROP_PASSWORD = "password";
    private static final String PROP_TOKEN = "token";
    private static final String PROP_DEVICE_ID = "deviceId";
    private static final String PROP_MFA_CHALLENGE_TOKEN = "mfaChallengeToken";
    private static final String PROP_TOTP_CODE = "totpCode";
    private static final String PROP_REFRESH_TOKEN = "refreshToken";
    private static final String PROP_NEW_PASSWORD = "newPassword";

    private static final String EXAMPLE_EMAIL = "user@example.com";
    private static final String EXAMPLE_PASSWORD = "VerySecurePassword123!";
    private static final String EXAMPLE_DEVICE_ID = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d";
    private static final String EXAMPLE_USER_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    private static final String EXAMPLE_REFRESH_TOKEN = "rt_3fa85f6457174562b3fc2c963f66afa6";
    private static final String REGEX_TOTP = "^\\d{6}$";

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
        components.addSchemas(SCHEMA_API_ERROR_RESPONSE, new ObjectSchema()
                .description("Standard API error payload")
                .addProperty("code", new StringSchema().example("AUTH_INVALID_CREDENTIALS"))
                .addProperty(PROP_MESSAGE, new StringSchema().example("Invalid email or password"))
                .addProperty("title", new StringSchema().example("Unauthorized"))
                .addProperty("status", new IntegerSchema().example(401))
                .addProperty("errors", new ObjectSchema().description("Field validation errors map, if applicable"))
        );

        // Requests
        components.addSchemas(SCHEMA_REGISTER_REQUEST, new ObjectSchema()
                .description("User registration request")
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addProperty(PROP_PASSWORD, new StringSchema().example(EXAMPLE_PASSWORD))
                .addRequiredItem(PROP_EMAIL).addRequiredItem(PROP_PASSWORD)
        );

        components.addSchemas(SCHEMA_EMAIL_VERIFY_REQUEST, new ObjectSchema()
                .description("Email verification dispatch request")
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addRequiredItem(PROP_EMAIL)
        );

        components.addSchemas(SCHEMA_EMAIL_VERIFY_CONFIRM_REQUEST, new ObjectSchema()
                .description("Email verification confirmation token request")
                .addProperty(PROP_TOKEN, new StringSchema().example("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"))
                .addRequiredItem(PROP_TOKEN)
        );

        components.addSchemas(SCHEMA_LOGIN_REQUEST, new ObjectSchema()
                .description("User login request")
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addProperty(PROP_PASSWORD, new StringSchema().example(EXAMPLE_PASSWORD))
                .addProperty(PROP_DEVICE_ID, new UUIDSchema().example(EXAMPLE_DEVICE_ID))
                .addRequiredItem(PROP_EMAIL).addRequiredItem(PROP_PASSWORD)
        );

        components.addSchemas(SCHEMA_VERIFY_MFA_REQUEST, new ObjectSchema()
                .description("MFA challenge verification request")
                .addProperty(PROP_MFA_CHALLENGE_TOKEN, new StringSchema().example("mfa_challenge_abc123"))
                .addProperty(PROP_TOTP_CODE, new StringSchema().example("123456").pattern(REGEX_TOTP))
                .addProperty(PROP_DEVICE_ID, new UUIDSchema().example(EXAMPLE_DEVICE_ID))
                .addRequiredItem(PROP_MFA_CHALLENGE_TOKEN).addRequiredItem(PROP_TOTP_CODE)
        );

        components.addSchemas(SCHEMA_REFRESH_TOKEN_REQUEST, new ObjectSchema()
                .description("Refresh token request")
                .addProperty(PROP_REFRESH_TOKEN, new StringSchema().example(EXAMPLE_REFRESH_TOKEN))
                .addProperty(PROP_DEVICE_ID, new UUIDSchema().example(EXAMPLE_DEVICE_ID))
                .addRequiredItem(PROP_REFRESH_TOKEN)
        );

        components.addSchemas(SCHEMA_LOGOUT_REQUEST, new ObjectSchema()
                .description("Logout session request")
                .addProperty(PROP_REFRESH_TOKEN, new StringSchema().example(EXAMPLE_REFRESH_TOKEN))
                .addProperty(PROP_DEVICE_ID, new UUIDSchema().example(EXAMPLE_DEVICE_ID))
                .addRequiredItem(PROP_REFRESH_TOKEN)
        );

        components.addSchemas(SCHEMA_FORGOT_PASSWORD_REQUEST, new ObjectSchema()
                .description("Forgot password email request")
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addRequiredItem(PROP_EMAIL)
        );

        components.addSchemas(SCHEMA_RESET_PASSWORD_REQUEST, new ObjectSchema()
                .description("Reset password with token request")
                .addProperty(PROP_TOKEN, new StringSchema().example("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"))
                .addProperty(PROP_NEW_PASSWORD, new StringSchema().example("NewVerySecurePassword123!"))
                .addRequiredItem(PROP_TOKEN).addRequiredItem(PROP_NEW_PASSWORD)
        );

        components.addSchemas(SCHEMA_CHANGE_PASSWORD_REQUEST, new ObjectSchema()
                .description("Change password request")
                .addProperty("currentPassword", new StringSchema().example("CurrentPassword123!"))
                .addProperty(PROP_NEW_PASSWORD, new StringSchema().example("NewVerySecurePassword123!"))
                .addRequiredItem("currentPassword").addRequiredItem(PROP_NEW_PASSWORD)
        );

        components.addSchemas(SCHEMA_CONFIRM_MFA_REQUEST, new ObjectSchema()
                .description("Confirm MFA enrollment request")
                .addProperty(PROP_TOTP_CODE, new StringSchema().example("654321").pattern(REGEX_TOTP))
                .addRequiredItem(PROP_TOTP_CODE)
        );

        components.addSchemas(SCHEMA_DISABLE_MFA_REQUEST, new ObjectSchema()
                .description("Disable MFA request")
                .addProperty(PROP_PASSWORD, new StringSchema().example(EXAMPLE_PASSWORD))
                .addProperty(PROP_TOTP_CODE, new StringSchema().example("654321").pattern(REGEX_TOTP))
                .addRequiredItem(PROP_PASSWORD).addRequiredItem(PROP_TOTP_CODE)
        );

        // Data Responses
        components.addSchemas("RegisterResponse", new ObjectSchema()
                .addProperty("userId", new UUIDSchema().example(EXAMPLE_USER_ID))
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addProperty(PROP_MESSAGE, new StringSchema().example("User registered successfully"))
        );

        components.addSchemas(SCHEMA_TOKEN_RESPONSE, new ObjectSchema()
                .addProperty("accessToken", new StringSchema().example("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."))
                .addProperty(PROP_REFRESH_TOKEN, new StringSchema().example(EXAMPLE_REFRESH_TOKEN))
                .addProperty("tokenType", new StringSchema().example("Bearer"))
                .addProperty("expiresIn", new IntegerSchema().example(300))
        );

        components.addSchemas("LoginResponse", new ObjectSchema()
                .addProperty("mfaRequired", new BooleanSchema().example(false))
                .addProperty(PROP_MFA_CHALLENGE_TOKEN, new StringSchema().example("mfa_challenge_abc123"))
                .addProperty("tokens", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + SCHEMA_TOKEN_RESPONSE))
        );

        components.addSchemas("MessageResponse", new ObjectSchema()
                .addProperty(PROP_MESSAGE, new StringSchema().example("Operation completed successfully"))
        );

        components.addSchemas("MeResponse", new ObjectSchema()
                .addProperty("id", new UUIDSchema().example(EXAMPLE_USER_ID))
                .addProperty(PROP_EMAIL, new StringSchema().example(EXAMPLE_EMAIL))
                .addProperty("status", new StringSchema().example("ACTIVE"))
                .addProperty("roles", new ArraySchema().items(new StringSchema().example("USER")))
                .addProperty("mfaEnabled", new BooleanSchema().example(true))
                .addProperty("createdAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-01-01T00:00:00Z"))
        );

        components.addSchemas(SCHEMA_SESSION_RESPONSE, new ObjectSchema()
                .addProperty("id", new UUIDSchema().example(EXAMPLE_USER_ID))
                .addProperty(PROP_DEVICE_ID, new UUIDSchema().example(EXAMPLE_DEVICE_ID))
                .addProperty("ipAddress", new StringSchema().example("192.168.1.100"))
                .addProperty("userAgent", new StringSchema().example("Mozilla/5.0"))
                .addProperty("createdAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T10:00:00Z"))
                .addProperty("lastActivityAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T10:05:00Z"))
                .addProperty("expiresAt", new StringSchema().format(FORMAT_DATE_TIME).example("2026-04-06T10:00:00Z"))
        );

        components.addSchemas("SessionsResponse", new ObjectSchema()
                .addProperty("sessions", new ArraySchema().items(new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + SCHEMA_SESSION_RESPONSE)))
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
        registerResponseWrapper(components, SCHEMA_TOKEN_API_RESPONSE, SCHEMA_TOKEN_RESPONSE);
        registerResponseWrapper(components, SCHEMA_MESSAGE_API_RESPONSE, "MessageResponse");
        registerResponseWrapper(components, "MeApiResponse", "MeResponse");
        registerResponseWrapper(components, "SessionsApiResponse", "SessionsResponse");
        registerResponseWrapper(components, "EnrollMfaApiResponse", "EnrollMfaResponse");

        components.addSchemas("ErrorApiResponse", new ObjectSchema()
                .description("Standard API error response wrapper")
                .addProperty("success", new BooleanSchema().example(false))
                .addProperty("data", new ObjectSchema().nullable(true).example(null))
                .addProperty("error", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + SCHEMA_API_ERROR_RESPONSE))
                .addProperty("timestamp", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T12:00:00Z"))
        );
    }

    private static void registerResponseWrapper(Components components, String wrapperName, String dataSchemaName) {
        components.addSchemas(wrapperName, new ObjectSchema()
                .description("Standard API response wrapper for " + dataSchemaName)
                .addProperty("success", new BooleanSchema().example(true))
                .addProperty("data", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + dataSchemaName))
                .addProperty("error", new Schema<>().$ref(OpenApiOperationEnum.SCHEMA_REF_PREFIX.value() + SCHEMA_API_ERROR_RESPONSE).nullable(true))
                .addProperty("timestamp", new StringSchema().format(FORMAT_DATE_TIME).example("2026-03-30T12:00:00Z"))
        );
    }

    private static void registerRoutes(Paths paths, String bearerScheme) {
        // Route 1: Register
        paths.addPathItem(OpenApiOperationEnum.PATH_REGISTER.value(), new PathItem().post(
                createOperation("register", "Register new user account",
                        "Registers a new user account with email and password, creates verification token and sends verification email event.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody(SCHEMA_REGISTER_REQUEST, "Registration payload with email and password"))
                        .responses(createResponses("201", "RegisterApiResponse", List.of("400", "409", "429")))
        ));

        // Route 2: Email verification request
        paths.addPathItem(OpenApiOperationEnum.PATH_EMAIL_VERIFY_REQUEST.value(), new PathItem().post(
                createOperation("requestEmailVerification", "Request email verification token",
                        "Requests a new verification token to be dispatched to the user's email address.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody(SCHEMA_EMAIL_VERIFY_REQUEST, "Email verification request payload"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "429")))
        ));

        // Route 3: Email verification confirmation
        paths.addPathItem(OpenApiOperationEnum.PATH_EMAIL_VERIFY_CONFIRM.value(), new PathItem().post(
                createOperation("confirmEmailVerification", "Confirm email verification",
                        "Validates the one-time token received via email and transitions user status to ACTIVE.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .requestBody(createRequestBody(SCHEMA_EMAIL_VERIFY_CONFIRM_REQUEST, "Confirmation payload containing verification token"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "404")))
        ));

        // Route 4: Login
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGIN.value(), new PathItem().post(
                createOperation("login", "Authenticate user credentials",
                        "Authenticates email and password. If MFA is active, returns an ephemeral challenge token; otherwise returns access/refresh token pair and creates a new session.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody(SCHEMA_LOGIN_REQUEST, "User credentials and optional device identifier"))
                        .responses(createResponses("200", "LoginApiResponse", List.of("400", "401", "403", "429")))
        ));

        // Route 5: MFA challenge verification
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_VERIFY.value(), new PathItem().post(
                createOperation("verifyMfa", "Verify MFA challenge during login",
                        "Validates a 6-digit TOTP code against the temporary MFA challenge token issued at login.",
                        OpenApiOperationEnum.TAG_MFA.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody(SCHEMA_VERIFY_MFA_REQUEST, "MFA challenge token and TOTP verification code"))
                        .responses(createResponses("200", SCHEMA_TOKEN_API_RESPONSE, List.of("400", "401", "429")))
        ));

        // Route 6: Refresh token rotation
        paths.addPathItem(OpenApiOperationEnum.PATH_REFRESH.value(), new PathItem().post(
                createOperation("refreshToken", "Refresh access and refresh tokens",
                        "Performs refresh token rotation: revokes the old refresh token and issues a new access/refresh token pair.",
                        OpenApiOperationEnum.TAG_AUTH.value(), null)
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_USER_AGENT.value()).description(OpenApiOperationEnum.HEADER_USER_AGENT_DESC.value()).required(false))
                        .addParametersItem(new HeaderParameter().name(OpenApiOperationEnum.HEADER_FORWARDED_FOR.value()).description(OpenApiOperationEnum.HEADER_FORWARDED_FOR_DESC.value()).required(false))
                        .requestBody(createRequestBody(SCHEMA_REFRESH_TOKEN_REQUEST, "Valid refresh token and optional device identifier"))
                        .responses(createResponses("200", SCHEMA_TOKEN_API_RESPONSE, List.of("400", "401")))
        ));

        // Route 7: Logout specific session
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGOUT.value(), new PathItem().post(
                createOperation("logout", "Logout specific session",
                        "Revokes the session and marks the refresh token as revoked.",
                        OpenApiOperationEnum.TAG_AUTH.value(), bearerScheme)
                        .requestBody(createRequestBody(SCHEMA_LOGOUT_REQUEST, "Refresh token to invalidate"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "401")))
        ));

        // Route 8: Logout all sessions
        paths.addPathItem(OpenApiOperationEnum.PATH_LOGOUT_ALL.value(), new PathItem().post(
                createOperation("logoutAll", "Logout all user sessions",
                        "Revokes all active sessions and refresh tokens belonging to the authenticated user.",
                        OpenApiOperationEnum.TAG_AUTH.value(), bearerScheme)
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("401")))
        ));

        // Route 9: Forgot password
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_FORGOT.value(), new PathItem().post(
                createOperation("forgotPassword", "Request password reset email",
                        "Generates a password reset token and sends an email notification if an account matches the email address.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), null)
                        .requestBody(createRequestBody(SCHEMA_FORGOT_PASSWORD_REQUEST, "Target account email address"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "429")))
        ));

        // Route 10: Reset password
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_RESET.value(), new PathItem().post(
                createOperation("resetPassword", "Reset password with token",
                        "Resets user password using the verification token and invalidates all existing sessions.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), null)
                        .requestBody(createRequestBody(SCHEMA_RESET_PASSWORD_REQUEST, "Reset token and new password"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400")))
        ));

        // Route 11: Change password
        paths.addPathItem(OpenApiOperationEnum.PATH_PASSWORD_CHANGE.value(), new PathItem().post(
                createOperation("changePassword", "Change password for authenticated user",
                        "Updates password for the currently logged-in user after verifying current credentials.",
                        OpenApiOperationEnum.TAG_PASSWORD.value(), bearerScheme)
                        .requestBody(createRequestBody(SCHEMA_CHANGE_PASSWORD_REQUEST, "Current and new password payload"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "401")))
        ));

        // Route 12: User profile
        paths.addPathItem(OpenApiOperationEnum.PATH_ME.value(), new PathItem().get(
                createOperation("getMe", "Get authenticated user profile",
                        "Retrieves the authenticated user's profile details including roles, status, and MFA status.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .responses(createResponses("200", "MeApiResponse", List.of("401")))
        ));

        // Route 13: List active user sessions
        paths.addPathItem(OpenApiOperationEnum.PATH_SESSIONS.value(), new PathItem().get(
                createOperation("getSessions", "List active user sessions",
                        "Returns a list of all active sessions for the authenticated user, including IP address, user agent, and activity timestamps.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .responses(createResponses("200", "SessionsApiResponse", List.of("401")))
        ));

        // Route 14: Revoke user session
        paths.addPathItem(OpenApiOperationEnum.PATH_SESSION_BY_ID.value(), new PathItem().delete(
                createOperation("revokeSession", "Revoke specific user session",
                        "Revokes a single active session identified by its session UUID.",
                        OpenApiOperationEnum.TAG_USER.value(), bearerScheme)
                        .addParametersItem(new PathParameter().name(OpenApiOperationEnum.PARAM_SESSION_ID.value()).description(OpenApiOperationEnum.PARAM_SESSION_ID_DESC.value()).required(true).schema(new UUIDSchema()))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("401", "404")))
        ));

        // Route 15: Enroll MFA
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_ENROLL.value(), new PathItem().post(
                createOperation("enrollMfa", "Enroll in TOTP MFA",
                        "Generates a new TOTP secret and otpauth URI with recovery codes for QR code scanning.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .responses(createResponses("200", "EnrollMfaApiResponse", List.of("401", "409")))
        ));

        // Route 16: Confirm MFA
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_CONFIRM.value(), new PathItem().post(
                createOperation("confirmMfa", "Confirm and activate TOTP MFA",
                        "Validates the initial 6-digit TOTP code and marks MFA as activated for the user.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .requestBody(createRequestBody(SCHEMA_CONFIRM_MFA_REQUEST, "TOTP confirmation code"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "401")))
        ));

        // Route 17: Disable MFA
        paths.addPathItem(OpenApiOperationEnum.PATH_MFA_DISABLE.value(), new PathItem().delete(
                createOperation("disableMfa", "Disable TOTP MFA",
                        "Disables MFA on user account after verifying current password and a valid 6-digit TOTP code.",
                        OpenApiOperationEnum.TAG_MFA.value(), bearerScheme)
                        .requestBody(createRequestBody(SCHEMA_DISABLE_MFA_REQUEST, "Password and TOTP code for MFA disable confirmation"))
                        .responses(createResponses("200", SCHEMA_MESSAGE_API_RESPONSE, List.of("400", "401")))
        ));

        // Route 18: Get JWKS
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
