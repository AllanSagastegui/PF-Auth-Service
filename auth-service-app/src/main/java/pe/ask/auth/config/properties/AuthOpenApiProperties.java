package pe.ask.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.ask.auth.config.model.OpenApiEnum;

@ConfigurationProperties(prefix = "app.openapi")
public record AuthOpenApiProperties(
        String title,
        String description,
        String version,
        String contactName,
        String contactEmail,
        String licenseName,
        String licenseUrl,
        String schemeBearer,
        String bearerFormat,
        String schemeNameBearer,
        String schemeDescription
) {
    public AuthOpenApiProperties {
        title = defaultIfBlank(title, OpenApiEnum.DEFAULT_TITLE);
        description = defaultIfBlank(description, OpenApiEnum.DEFAULT_DESCRIPTION);
        version = defaultIfBlank(version, OpenApiEnum.DEFAULT_VERSION);
        contactName = defaultIfBlank(contactName, OpenApiEnum.DEFAULT_CONTACT_NAME);
        contactEmail = defaultIfBlank(contactEmail, OpenApiEnum.DEFAULT_CONTACT_EMAIL);
        licenseName = defaultIfBlank(licenseName, OpenApiEnum.DEFAULT_LICENSE_NAME);
        licenseUrl = defaultIfBlank(licenseUrl, OpenApiEnum.DEFAULT_LICENSE_URL);
        schemeBearer = defaultIfBlank(schemeBearer, OpenApiEnum.DEFAULT_SCHEME_BEARER);
        bearerFormat = defaultIfBlank(bearerFormat, OpenApiEnum.DEFAULT_BEARER_FORMAT);
        schemeNameBearer = defaultIfBlank(schemeNameBearer, OpenApiEnum.DEFAULT_SCHEME_NAME_BEARER);
        schemeDescription = defaultIfBlank(schemeDescription, OpenApiEnum.DEFAULT_SCHEME_DESCRIPTION);
    }

    private static String defaultIfBlank(String value, OpenApiEnum fallback) {
        return (value != null && !value.isBlank()) ? value : fallback.value();
    }
}
