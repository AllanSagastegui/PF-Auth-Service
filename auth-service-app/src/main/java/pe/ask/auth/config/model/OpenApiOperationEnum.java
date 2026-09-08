package pe.ask.auth.config.model;

import pe.ask.auth.input.api.filter.ApiPathEnum;

public enum OpenApiOperationEnum {
    // Tags
    TAG_AUTH("Authentication & Registration"),
    TAG_PASSWORD("Password Management"),
    TAG_USER("User Profile & Sessions"),
    TAG_MFA("Multi-Factor Authentication"),
    TAG_JWKS("JWKS & Cryptography"),

    // Headers & Parameters
    HEADER_CORRELATION_ID("X-Correlation-Id"),
    HEADER_CORRELATION_ID_DESC("Distributed tracing correlation identifier"),
    HEADER_REQUEST_ID("X-Request-Id"),
    HEADER_REQUEST_ID_DESC("Unique request identifier"),
    HEADER_USER_AGENT("User-Agent"),
    HEADER_USER_AGENT_DESC("Client User-Agent header"),
    HEADER_FORWARDED_FOR("X-Forwarded-For"),
    HEADER_FORWARDED_FOR_DESC("Client IP address"),
    PARAM_SESSION_ID("sessionId"),
    PARAM_SESSION_ID_DESC("Unique session identifier UUID"),

    // Content types
    CONTENT_TYPE_JSON("application/json"),
    SCHEMA_REF_PREFIX("#/components/schemas/"),

    // Response descriptions
    STATUS_200_DESC("Request processed successfully"),
    STATUS_201_DESC("Resource created successfully"),
    STATUS_400_DESC("Bad Request - Validation error or malformed body"),
    STATUS_401_DESC("Unauthorized - Missing, invalid, or expired credentials"),
    STATUS_403_DESC("Forbidden - Insufficient permissions or unverified account"),
    STATUS_404_DESC("Not Found - Requested resource does not exist"),
    STATUS_409_DESC("Conflict - Resource state conflict or already registered"),
    STATUS_429_DESC("Too Many Requests - Rate limit exceeded"),

    // Route paths matching ApiPathEnum
    PATH_REGISTER(ApiPathEnum.REGISTER.value()),
    PATH_EMAIL_VERIFY_REQUEST(ApiPathEnum.EMAIL_VERIFY_REQUEST.value()),
    PATH_EMAIL_VERIFY_CONFIRM(ApiPathEnum.EMAIL_VERIFY_CONFIRM.value()),
    PATH_LOGIN(ApiPathEnum.LOGIN.value()),
    PATH_MFA_VERIFY(ApiPathEnum.VERIFY_MFA.value()),
    PATH_REFRESH(ApiPathEnum.REFRESH.value()),
    PATH_LOGOUT(ApiPathEnum.LOGOUT.value()),
    PATH_LOGOUT_ALL(ApiPathEnum.LOGOUT_ALL.value()),
    PATH_PASSWORD_FORGOT(ApiPathEnum.FORGOT_PASSWORD.value()),
    PATH_PASSWORD_RESET(ApiPathEnum.RESET_PASSWORD.value()),
    PATH_PASSWORD_CHANGE(ApiPathEnum.CHANGE_PASSWORD.value()),
    PATH_ME(ApiPathEnum.ME.value()),
    PATH_SESSIONS(ApiPathEnum.SESSIONS.value()),
    PATH_SESSION_BY_ID(ApiPathEnum.SESSION_BY_ID.value()),
    PATH_MFA_ENROLL(ApiPathEnum.MFA_ENROLL.value()),
    PATH_MFA_CONFIRM(ApiPathEnum.MFA_CONFIRM.value()),
    PATH_MFA_DISABLE(ApiPathEnum.MFA_DISABLE.value()),
    PATH_JWKS(ApiPathEnum.JWKS.value());

    private final String value;

    OpenApiOperationEnum(String value) {
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
