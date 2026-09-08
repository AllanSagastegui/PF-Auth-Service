package pe.ask.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pe.ask.auth.config.properties.AuthKafkaProperties;
import pe.ask.auth.config.properties.AuthOpenApiProperties;
import pe.ask.auth.config.properties.AuthSecurityProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        AuthSecurityProperties.class,
        AuthOpenApiProperties.class,
        AuthKafkaProperties.class
})
public final class CorePropertiesConfiguration {
}
