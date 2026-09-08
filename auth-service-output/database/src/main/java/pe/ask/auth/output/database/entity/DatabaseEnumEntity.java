package pe.ask.auth.output.database.entity;

public enum DatabaseEnumEntity {
    TABLE_USERS("users"),
    TABLE_USER_CREDENTIALS("user_credentials"),
    TABLE_ROLES("roles"),
    TABLE_USER_ROLES("user_roles"),
    TABLE_SESSIONS("sessions"),
    TABLE_REFRESH_TOKENS("refresh_tokens"),
    TABLE_ONE_TIME_TOKENS("one_time_tokens"),
    TABLE_OUTBOX_EVENTS("outbox_events"),

    COL_ID("id"),
    COL_EMAIL("email"),
    COL_STATUS("status"),
    COL_MFA_ENABLED("mfa_enabled"),
    COL_MFA_SECRET("mfa_secret"),
    COL_VERSION("version"),
    COL_CREATED_AT("created_at"),
    COL_UPDATED_AT("updated_at"),
    COL_USER_ID("user_id"),
    COL_PASSWORD_HASH("password_hash"),
    COL_FAILED_ATTEMPTS("failed_attempts"),
    COL_LOCKED_UNTIL("locked_until"),
    COL_PASSWORD_CHANGED_AT("password_changed_at"),
    COL_DEVICE_ID("device_id"),
    COL_IP_ADDRESS("ip_address"),
    COL_USER_AGENT("user_agent"),
    COL_LAST_ACTIVITY_AT("last_activity_at"),
    COL_EXPIRES_AT("expires_at"),
    COL_REVOKED_AT("revoked_at"),
    COL_SESSION_ID("session_id"),
    COL_TOKEN_HASH("token_hash"),
    COL_REPLACED_BY_HASH("replaced_by_hash"),
    COL_FAMILY_ID("family_id"),
    COL_TYPE("type"),
    COL_USED_AT("used_at"),
    COL_NAME("name"),
    COL_ROLE_ID("role_id"),
    COL_ROLE_NAME("role_name"),
    COL_AGGREGATE_TYPE("aggregate_type"),
    COL_AGGREGATE_ID("aggregate_id"),
    COL_EVENT_TYPE("event_type"),
    COL_PAYLOAD("payload"),
    COL_RETRIES("retries"),

    FIELD_ID("id"),
    FIELD_CREATED_AT("createdAt"),
    FIELD_UPDATED_AT("updatedAt"),
    FIELD_VERSION("version"),

    ORDER_ASC("ASC"),
    ORDER_DESC("DESC"),

    MSG_MAPPER_NOT_CONFIGURED("Mappers not configured for ReactiveOperationsHelperRepository"),
    MSG_ENTITY_NOT_FOUND_PREFIX("Entity not found for id: "),

    PARAM_USER("user"),
    PARAM_CREDENTIALS("credentials"),
    PARAM_USER_ID("userId"),
    PARAM_DEVICE_ID("deviceId"),
    PARAM_TOKEN("token"),
    PARAM_EVENT("event"),
    PARAM_SESSION("session"),
    PARAM_DOMAIN("domain"),
    PARAM_DOMAIN_FLUX("domainFlux"),
    PARAM_ID("id"),
    PARAM_SORT_BY("sortBy"),
    PARAM_SORT_DIRECTION("sortDirection"),
    PARAM_PATCH_DOMAIN("patchDomain"),
    PARAM_ENTITY("entity"),
    PARAM_REPOSITORY("repository"),
    PARAM_TOKEN_HASH("tokenHash"),
    PARAM_REFRESH_TOKEN("refreshToken"),
    PARAM_FAMILY_ID("familyId"),
    PARAM_CANONICAL_EMAIL("canonicalEmail"),
    PARAM_ONE_TIME_TOKEN("oneTimeToken"),
    PARAM_OUTBOX_EVENT("message"),
    PARAM_NOW("now");

    private final String value;

    DatabaseEnumEntity(String value) {
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
