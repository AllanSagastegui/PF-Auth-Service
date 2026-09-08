package pe.ask.auth.output.security.model;

public enum SecurityEnum {
    RAW_PASSWORD("rawPassword"),
    PASSWORD_HASH("passwordHash"),
    SCHEDULER_NAME("argon2-worker"),
    DUMMY_PASSWORD("dummy-password-for-anti-enumeration-timing"),
    DEFAULT_KEY_ID("ask-auth-key-1"),
    DEFAULT_ISSUER("pe.ask.auth"),
    ISSUER("pe.ask.auth"),
    AUDIENCE("pe.ask.api"),
    KEY_ID("ask-auth-key-1"),
    TOKEN_TYPE_JWT("at+jwt"),
    SCOPE_OPENID_PROFILE("openid profile"),
    HASH_ALGORITHM_SHA256("SHA-256"),
    CIPHER_AES_GCM_NO_PADDING("AES/GCM/NoPadding"),
    ALGORITHM_AES("AES"),
    ALGORITHM_HMAC_SHA1("HmacSHA1"),
    BASE32_ALPHABET("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
    OTP_ISSUER_NAME("AskAuth"),
    OTP_SCHEME_PREFIX("otpauth://totp/"),
    CLAIM_SID("sid"),
    CLAIM_ROLES("roles"),
    CLAIM_SCOPE("scope"),
    CLAIM_VER("ver"),
    CLAIM_AMR("amr"),
    CLAIM_AUTH_TIME("auth_time"),
    PARAM_RAW_PASSWORD("rawPassword"),
    PARAM_PASSWORD_HASH("passwordHash"),
    PARAM_USER_ID("userId"),
    PARAM_ROLES("roles"),
    PARAM_TOKEN("token"),
    PARAM_TOKEN_HASH("tokenHash"),
    PARAM_SECRET("secret"),
    PARAM_CODE("code"),
    PARAM_CONFIG("config"),
    ERR_INIT_TOKEN_GENERATOR("Failed to initialize TokenGeneratorAdapter"),
    ERR_ENCRYPT_TOTP("Failed to encrypt TOTP secret"),
    ERR_DECRYPT_TOTP("Failed to decrypt TOTP secret"),
    ERR_INVALID_ENCRYPTED_PAYLOAD("Invalid encrypted payload length");

    private final String value;

    SecurityEnum(String value) {
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
