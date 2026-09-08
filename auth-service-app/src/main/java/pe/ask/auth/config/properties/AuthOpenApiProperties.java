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
        title = (title != null && !title.isBlank()) ? title : OpenApiEnum.DEFAULT_TITLE.value();
        description = (description != null && !description.isBlank()) ? description : OpenApiEnum.DEFAULT_DESCRIPTION.value();
        version = (version != null && !version.isBlank()) ? version : OpenApiEnum.DEFAULT_VERSION.value();
        contactName = (contactName != null && !contactName.isBlank()) ? contactName : OpenApiEnum.DEFAULT_CONTACT_NAME.value();
        contactEmail = (contactEmail != null && !contactEmail.isBlank()) ? contactEmail : OpenApiEnum.DEFAULT_CONTACT_EMAIL.value();
        licenseName = (licenseName != null && !licenseName.isBlank()) ? licenseName : OpenApiEnum.DEFAULT_LICENSE_NAME.value();
        licenseUrl = (licenseUrl != null && !licenseUrl.isBlank()) ? licenseUrl : OpenApiEnum.DEFAULT_LICENSE_URL.value();
        schemeBearer = (schemeBearer != null && !schemeBearer.isBlank()) ? schemeBearer : OpenApiEnum.DEFAULT_SCHEME_BEARER.value();
        bearerFormat = (bearerFormat != null && !bearerFormat.isBlank()) ? bearerFormat : OpenApiEnum.DEFAULT_BEARER_FORMAT.value();
        schemeNameBearer = (schemeNameBearer != null && !schemeNameBearer.isBlank()) ? schemeNameBearer : OpenApiEnum.DEFAULT_SCHEME_NAME_BEARER.value();
        schemeDescription = (schemeDescription != null && !schemeDescription.isBlank()) ? schemeDescription : OpenApiEnum.DEFAULT_SCHEME_DESCRIPTION.value();
    }
}
