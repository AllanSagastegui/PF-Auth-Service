package pe.ask.auth.input.api.filter;

public enum ApiHeaderEnum {
    CORRELATION_ID("X-Correlation-Id"),
    REQUEST_ID("X-Request-Id"),
    IDEMPOTENCY_KEY("Idempotency-Key"),
    AUTHORIZATION("Authorization"),
    USER_AGENT("User-Agent"),
    FORWARDED_FOR("X-Forwarded-For"),
    USER_ID("X-User-Id"),
    LOCATION("Location"),
    CONTENT_TYPE("Content-Type"),
    BEARER_PREFIX("Bearer "),
    DEFAULT_IP("127.0.0.1"),
    DEFAULT_USER_AGENT("unknown");

    private final String value;

    ApiHeaderEnum(String value) {
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
