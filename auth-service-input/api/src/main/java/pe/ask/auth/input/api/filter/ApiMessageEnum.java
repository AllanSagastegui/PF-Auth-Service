package pe.ask.auth.input.api.filter;

public enum ApiMessageEnum {
    AUTHENTICATION_REQUIRED("Authentication required"),
    INVALID_USER_ID_HEADER("Invalid user ID header"),
    INVALID_REQUEST_BODY("Invalid request body"),
    VALIDATION_FAILED("Validation failed"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred"),
    TITLE_INTERNAL_SERVER_ERROR("Internal Server Error"),
    TITLE_VALIDATION_FAILED("Validation Failed"),
    TITLE_UNAUTHORIZED("Unauthorized"),
    ERROR_CODE_INTERNAL_ERROR("AUTH_INTERNAL_ERROR"),
    ERROR_CODE_VALIDATION_ERROR("AUTH_VALIDATION_ERROR"),
    ERROR_CODE_UNAUTHORIZED("AUTH_UNAUTHORIZED"),
    CONTEXT_CORRELATION_ID("correlationId"),
    CONTEXT_REQUEST_ID("requestId"),
    PROP_TYPE("type"),
    PROP_ABOUT_BLANK("about:blank"),
    PROP_TITLE("title"),
    PROP_STATUS("status"),
    PROP_DETAIL("detail"),
    PROP_INSTANCE("instance"),
    PROP_CODE("code"),
    PROP_ERROR_CODE("error_code"),
    PROP_CORRELATION_ID("correlationId"),
    PROP_RETRYABLE("retryable"),
    PROP_TIMESTAMP("timestamp"),
    PROP_ERRORS("errors"),
    PROP_VIOLATIONS("violations"),
    PROP_FIELD("field"),
    PROP_MESSAGE("message"),
    PARAM_ARGUMENT("argument");

    private final String value;

    ApiMessageEnum(String value) {
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
