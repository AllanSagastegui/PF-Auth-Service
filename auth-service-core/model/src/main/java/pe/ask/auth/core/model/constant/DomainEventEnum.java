package pe.ask.auth.core.model.constant;

public enum DomainEventEnum {
    USER_REGISTERED("auth.user.registered.v1"),
    AGGREGATE_TYPE_USER("USER"),
    AGGREGATE_TYPE_SESSION("SESSION"),
    AMR_PASSWORD("pwd"),
    AMR_MFA("mfa"),
    TOKEN_TYPE_BEARER("Bearer"),
    SYSTEM_KEY("system");

    private final String value;

    DomainEventEnum(String value) {
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
