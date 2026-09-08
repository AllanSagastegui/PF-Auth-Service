package pe.ask.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
@ConfigurationPropertiesScan
public final class MainApplication {
    public static void main(String[] args) {
        log.info("Starting Main Application");
        SpringApplication application = new SpringApplication(MainApplication.class);
        application.setWebApplicationType(WebApplicationType.REACTIVE);
        application.run(args);
    }
}
