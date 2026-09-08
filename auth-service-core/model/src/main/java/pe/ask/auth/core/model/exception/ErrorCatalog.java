package pe.ask.auth.core.model.exception;

import java.util.Map;

public enum ErrorCatalog {

    /*
     * Generic errors
     */
    VALIDATION_EXCEPTION(
            "AUTH_VALIDATION_ERROR",
            "Validation Failed",
            "Oops! Some of the data you sent doesn’t look right. Please review the fields and try again.",
            400,
            null
    ),
    REQUIRED_ARGUMENT(
            "AUTH_REQUIRED_ARGUMENT",
            "RequiredArgumentException",
            "A required argument was null or missing.",
            400,
            null
    ),
    INTERNAL_SERVER_ERROR(
            "AUTH_INTERNAL_ERROR",
            "Internal Server Error",
            "Something went wrong on our side. Please try again later or contact support if the issue persists.",
            500,
            Map.of("server", "Unexpected error occurred")
    ),

    /*
     * Authentication and Authorization domain errors
     */
    USER_ALREADY_EXISTS(
            "AUTH_USER_ALREADY_EXISTS",
            "UserAlreadyExistsException",
            "A user with the same email already exists.",
            409,
            null
    ),
    USER_NOT_FOUND(
            "AUTH_USER_NOT_FOUND",
            "UserNotFoundException",
            "The requested user could not be found.",
            404,
            null
    ),
    INVALID_CREDENTIALS(
            "AUTH_INVALID_CREDENTIALS",
            "InvalidCredentialsException",
            "Invalid email or password provided.",
            401,
            null
    ),
    EMAIL_NOT_VERIFIED(
            "AUTH_EMAIL_NOT_VERIFIED",
            "EmailNotVerifiedException",
            "Email address has not been verified. Please check your inbox or request a new verification link.",
            403,
            null
    ),
    INVALID_TOKEN(
            "AUTH_ACCESS_TOKEN_INVALID",
            "InvalidTokenException",
            "The provided authentication token is invalid or malformed.",
            401,
            null
    ),
    TOKEN_EXPIRED(
            "AUTH_ACCESS_TOKEN_EXPIRED",
            "TokenExpiredException",
            "The authentication token has expired.",
            401,
            null
    ),
    UNAUTHORIZED(
            "AUTH_UNAUTHORIZED",
            "UnauthorizedException",
            "Authentication is required to access this resource.",
            401,
            null
    ),
    FORBIDDEN(
            "AUTH_FORBIDDEN",
            "ForbiddenException",
            "You do not have permission to access this resource.",
            403,
            null
    ),
    ACCOUNT_LOCKED(
            "AUTH_ACCOUNT_LOCKED",
            "AccountLockedException",
            "The user account has been locked due to security policies.",
            423,
            null
    ),
    ACCOUNT_DISABLED(
            "AUTH_ACCOUNT_UNAVAILABLE",
            "AccountDisabledException",
            "The user account has been disabled.",
            403,
            null
    ),
    PASSWORD_POLICY(
            "AUTH_PASSWORD_POLICY",
            "PasswordPolicyException",
            "Password does not meet the security policy requirements.",
            422,
            null
    ),
    IDEMPOTENCY_CONFLICT(
            "AUTH_IDEMPOTENCY_CONFLICT",
            "IdempotencyConflictException",
            "Idempotency conflict: concurrent operation in progress or conflicting payload.",
            409,
            null
    ),
    RATE_LIMITED(
            "AUTH_RATE_LIMITED",
            "RateLimitedException",
            "Too many requests. Please slow down and try again later.",
            429,
            null
    );

    private final String errorCode;
    private final String exceptionName;
    private final String message;
    private final int status;
    private final Map<String, String> errors;

    ErrorCatalog(String errorCode, String exceptionName, String message, int status, Map<String, String> errors) {
        this.errorCode = errorCode;
        this.exceptionName = exceptionName;
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
