package pe.ask.auth.config.model;

public enum OpenApiEnum {
    DEFAULT_TITLE("Ask Auth Service API"),
    DEFAULT_DESCRIPTION("High-performance reactive authentication and authorization microservice using WebFlux, R2DBC, and Redis."),
    DEFAULT_VERSION("v1"),
    DEFAULT_CONTACT_NAME("Ask Engineering Team"),
    DEFAULT_CONTACT_EMAIL("security@ask.pe"),
    DEFAULT_LICENSE_NAME("Apache 2.0"),
    DEFAULT_LICENSE_URL("https://www.apache.org/licenses/LICENSE-2.0"),
    DEFAULT_SCHEME_BEARER("bearerAuth"),
    DEFAULT_BEARER_FORMAT("JWT"),
    DEFAULT_SCHEME_NAME_BEARER("bearer"),
    DEFAULT_SCHEME_DESCRIPTION("JWT Bearer token for authorized requests");

    private final String value;

    OpenApiEnum(String value) {
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
