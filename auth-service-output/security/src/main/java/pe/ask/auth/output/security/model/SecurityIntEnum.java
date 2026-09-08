package pe.ask.auth.output.security.model;

public enum SecurityIntEnum {
    REFRESH_TOKEN_BYTES(32),
    ACCESS_TOKEN_TTL_SECONDS(300),
    MFA_CHALLENGE_TTL_SECONDS(300),
    TOTP_TIME_STEP_SECONDS(30),
    TOTP_CODE_DIGITS(6),
    GCM_TAG_LENGTH(128),
    GCM_IV_LENGTH(12),
    DEFAULT_SALT_LENGTH(16),
    DEFAULT_HASH_LENGTH(32),
    DEFAULT_PARALLELISM(1),
    DEFAULT_MEMORY_KIB(19456),
    DEFAULT_ITERATIONS(2),
    DEFAULT_SCHEDULER_THREADS(4);

    private final int value;

    SecurityIntEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
