package pe.ask.auth.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.ask.auth.config.properties.AuthOpenApiProperties;

@Configuration(proxyBeanMethods = false)
public final class OpenApiConfiguration {

    static {
        Json.mapper().registerModule(new JavaTimeModule());
        Yaml.mapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public OpenAPI customOpenAPI(AuthOpenApiProperties properties) {
        AuthOpenApiProperties props = properties != null ? properties : new AuthOpenApiProperties(
                null, null, null, null, null, null, null, null, null, null, null
        );

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(props.title())
                        .description(props.description())
                        .version(props.version())
                        .contact(new Contact().name(props.contactName()).email(props.contactEmail()))
                        .license(new License().name(props.licenseName()).url(props.licenseUrl())))
                .components(new Components()
                        .addSecuritySchemes(props.schemeBearer(), new SecurityScheme()
                                .name(props.schemeBearer())
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(props.schemeNameBearer())
                                .bearerFormat(props.bearerFormat())
                                .description(props.schemeDescription())));

        OpenApiSpecificationFactory.populateSpecification(openAPI, props.schemeBearer());
        return openAPI;
    }
}
