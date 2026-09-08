package pe.ask.auth.config;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration(proxyBeanMethods = false)
public final class DatabaseInitializerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializerConfiguration.class);

    @Bean
    public ConnectionFactoryInitializer connectionFactoryInitializer(
            @Qualifier("authConnectionFactory") ConnectionFactory connectionFactory) {
        log.info("Registering R2DBC ConnectionFactoryInitializer with classpath:schema.sql");
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(true);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
