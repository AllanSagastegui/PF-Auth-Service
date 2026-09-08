package pe.ask.auth.core.model.constant;

public enum DomainMessageEnum {
    ARGUMENT_MUST_NOT_BE_NULL("must not be null"),
    REGISTRATION_SUCCESS("Registration successful. Please verify your email."),
    EMAIL_VERIFICATION_SENT("If the account exists and is pending verification, a new link has been sent."),
    EMAIL_VERIFIED_SUCCESS("Email verified successfully."),
    PASSWORD_RESET_SENT("If an account with that email exists, password reset instructions have been sent."),
    PASSWORD_RESET_SUCCESS("Password has been successfully reset."),
    PASSWORD_CHANGED_SUCCESS("Password changed successfully."),
    SESSION_REVOKED_SUCCESS("Session revoked successfully"),
    LOGGED_OUT_SUCCESS("Logged out successfully"),
    LOGGED_OUT_ALL_SUCCESS("All active sessions have been revoked."),
    MFA_DISABLED_SUCCESS("MFA has been successfully disabled."),
    MFA_ACTIVATED_SUCCESS("MFA configured and activated successfully"),
    INVALID_EMAIL("Email must be a valid email address"),
    INVALID_PASSWORD_LENGTH("Password must be between 12 and 128 characters"),
    PASSWORD_UPPERCASE_REQUIRED("Password must contain at least one uppercase letter"),
    PASSWORD_LOWERCASE_REQUIRED("Password must contain at least one lowercase letter"),
    PASSWORD_DIGIT_REQUIRED("Password must contain at least one digit"),
    PASSWORD_SPECIAL_CHAR_REQUIRED("Password must contain at least one special character");

    private final String value;

    DomainMessageEnum(String value) {
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
