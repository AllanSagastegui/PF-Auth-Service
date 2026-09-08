package pe.ask.auth.core.model.constant;

public enum DomainFieldEnum {
    EMAIL("email"),
    PASSWORD("password"),
    RAW_EMAIL("rawEmail"),
    USER_ID("userId"),
    CODE("code"),
    TOTP_CODE("totpCode"),
    TOKEN("token"),
    SESSION_ID("sessionId"),
    REFRESH_TOKEN("refreshToken"),
    NEW_PASSWORD("newPassword"),
    OLD_PASSWORD("oldPassword"),
    STATUS("status"),
    ACCESS_TOKEN("accessToken"),
    RAW_REFRESH_TOKEN("rawRefreshToken"),
    TOKEN_TYPE("tokenType"),
    BEARER("Bearer"),
    SECRET_ENCRYPTED("secretEncrypted"),
    ARGUMENT("argument"),
    ARGUMENT_PREFIX("Argument '"),
    ARGUMENT_SUFFIX("' ");

    private final String value;

    DomainFieldEnum(String value) {
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
