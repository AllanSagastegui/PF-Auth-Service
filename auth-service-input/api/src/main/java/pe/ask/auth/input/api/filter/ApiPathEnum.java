package pe.ask.auth.input.api.filter;

public enum ApiPathEnum {
    AUTH_PREFIX("/api/v1/auth"),
    REGISTER("/api/v1/auth/register"),
    LOGIN("/api/v1/auth/login"),
    VERIFY_MFA("/api/v1/auth/mfa/totp/verify"),
    REFRESH("/api/v1/auth/refresh"),
    LOGOUT("/api/v1/auth/logout"),
    LOGOUT_ALL("/api/v1/auth/logout-all"),
    FORGOT_PASSWORD("/api/v1/auth/password/forgot"),
    RESET_PASSWORD("/api/v1/auth/password/reset"),
    CHANGE_PASSWORD("/api/v1/auth/password/change"),
    EMAIL_VERIFY_REQUEST("/api/v1/auth/email-verification/requests"),
    EMAIL_VERIFY_CONFIRM("/api/v1/auth/email-verification/confirmations"),
    ME("/api/v1/auth/me"),
    SESSIONS("/api/v1/auth/sessions"),
    SESSION_BY_ID("/api/v1/auth/sessions/{sessionId}"),
    MFA_ENROLL("/api/v1/auth/mfa/totp/enrollment"),
    MFA_CONFIRM("/api/v1/auth/mfa/totp/confirmation"),
    MFA_DISABLE("/api/v1/auth/mfa/totp"),
    JWKS("/.well-known/jwks.json"),

    SUB_REGISTER("/register"),
    SUB_EMAIL_VERIFY_REQUEST("/email-verification/requests"),
    SUB_EMAIL_VERIFY_CONFIRM("/email-verification/confirmations"),
    SUB_LOGIN("/login"),
    SUB_VERIFY_MFA("/mfa/totp/verify"),
    SUB_REFRESH("/refresh"),
    SUB_LOGOUT("/logout"),
    SUB_LOGOUT_ALL("/logout-all"),
    SUB_FORGOT_PASSWORD("/password/forgot"),
    SUB_RESET_PASSWORD("/password/reset"),
    SUB_CHANGE_PASSWORD("/password/change"),
    SUB_ME("/me"),
    SUB_SESSIONS("/sessions"),
    SUB_SESSION_BY_ID("/sessions/{sessionId}"),
    SUB_MFA_ENROLL("/mfa/totp/enrollment"),
    SUB_MFA_CONFIRM("/mfa/totp/confirmation"),
    SUB_MFA_DISABLE("/mfa/totp");

    private final String value;

    ApiPathEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
