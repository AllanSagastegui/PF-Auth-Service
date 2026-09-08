package pe.ask.auth.core.model.constant;

public final class DomainConstants {

    private DomainConstants() {
        // Prevent instantiation
    }

    public static final String MSG_ARGUMENT_MUST_NOT_BE_NULL = "must not be null";
    public static final String MSG_REGISTRATION_SUCCESS = "Registration successful. Please verify your email.";
    public static final String MSG_EMAIL_VERIFICATION_SENT = "If the account exists and is pending verification, a new link has been sent.";
    public static final String MSG_EMAIL_VERIFIED_SUCCESS = "Email verified successfully.";
    public static final String MSG_PASSWORD_RESET_SENT = "If an account with that email exists, password reset instructions have been sent.";
    public static final String MSG_PASSWORD_RESET_SUCCESS = "Password has been successfully reset.";
    public static final String MSG_PASSWORD_CHANGED_SUCCESS = "Password changed successfully.";
    public static final String MSG_SESSION_REVOKED_SUCCESS = "Session revoked successfully";
    public static final String MSG_LOGGED_OUT_SUCCESS = "Logged out successfully";
    public static final String MSG_LOGGED_OUT_ALL_SUCCESS = "All active sessions have been revoked.";
    public static final String MSG_MFA_DISABLED_SUCCESS = "MFA has been successfully disabled.";
    public static final String MSG_MFA_ACTIVATED_SUCCESS = "MFA configured and activated successfully";

    public static final String MSG_INVALID_EMAIL = "Email must be a valid email address";
    public static final String MSG_INVALID_PASSWORD_LENGTH = "Password must be between 12 and 128 characters";
    public static final String MSG_PASSWORD_UPPERCASE_REQUIRED = "Password must contain at least one uppercase letter";
    public static final String MSG_PASSWORD_LOWERCASE_REQUIRED = "Password must contain at least one lowercase letter";
    public static final String MSG_PASSWORD_DIGIT_REQUIRED = "Password must contain at least one digit";
    public static final String MSG_PASSWORD_SPECIAL_CHAR_REQUIRED = "Password must contain at least one special character";

    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final String EVENT_USER_REGISTERED_V1 = "auth.user.registered.v1";
    public static final String AGGREGATE_TYPE_USER = "USER";
    public static final String AMR_PASSWORD = "pwd";
    public static final String AMR_MFA = "mfa";

    public static final long DEFAULT_ACCESS_TOKEN_TTL_SECONDS = 900L;
    public static final long DEFAULT_MFA_CHALLENGE_TTL_SECONDS = 300L;

    public static final String TOPIC_NOTIFICATIONS = "auth.notifications";
    public static final String TOPIC_AUDIT = "auth.audit";

    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_TRACE_ID = "traceId";
    public static final String HEADER_TIMESTAMP = "timestamp";
    public static final String HEADER_EVENT_TYPE = "eventType";

    public static final String CONTEXT_CORRELATION_ID = "correlationId";
    public static final String CONTEXT_REQUEST_ID = "requestId";
    public static final String SYSTEM_KEY = "system";
}
