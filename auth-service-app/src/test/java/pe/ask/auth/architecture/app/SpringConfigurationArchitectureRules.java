package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG_PROPERTIES;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_DTO;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_ENTITY;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_COMMAND;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_RESULT;

public final class SpringConfigurationArchitectureRules {

    private SpringConfigurationArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Toda configuración Spring debe estar en pe.ask.auth.config.
     * Los routers del input no deben ser @Configuration.
     * Deben ser clases normales registradas desde el módulo app.
     */
    @ArchTest
    static final ArchRule SPRING_CONFIGURATION_MUST_RESIDE_IN_APP_CONFIG =
            classes()
                    .that()
                    .areAnnotatedWith(Configuration.class)
                    .should()
                    .resideInAPackage(CONFIG)
                    .andShould()
                    .haveSimpleNameEndingWith("Configuration")
                    .andShould()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "toda configuración Spring debe permanecer "
                                    + "centralizada en el composition root"
                    );

    /**
     * Las properties deben:
     *
     * - Estar dentro de config.properties.
     * - Terminar en Properties.
     */
    @ArchTest
    static final ArchRule CONFIGURATION_PROPERTIES_MUST_HAVE_CORRECT_LOCATION =
            classes()
                    .that()
                    .areAnnotatedWith(ConfigurationProperties.class)
                    .should()
                    .resideInAPackage(CONFIG_PROPERTIES)
                    .andShould()
                    .haveSimpleNameEndingWith("Properties")
                    .because(
                            "las propiedades externas deben estar organizadas "
                                    + "en config.properties"
                    )
                    .allowEmptyShould(true);

    /**
     * Las @ConfigurationProperties no deben usar @Component.
     *
     * Deben registrarse mediante @ConfigurationPropertiesScan
     * desde AuthApplication.
     */
    @ArchTest
    static final ArchRule CONFIGURATION_PROPERTIES_MUST_NOT_BE_COMPONENTS =
            noClasses()
                    .that()
                    .areAnnotatedWith(ConfigurationProperties.class)
                    .should()
                    .beAnnotatedWith(Component.class)
                    .because(
                            "las configuration properties deben registrarse "
                                    + "mediante scanning y no mediante component scanning"
                    )
                    .allowEmptyShould(true);

    /**
     * Todas las configuraciones deben desactivar el proxy de métodos.
     *
     * Deben declararse así:
     *
     * @Configuration(proxyBeanMethods = false)
     */
    @ArchTest
    static void CONFIGURATION_CLASSES_MUST_DISABLE_PROXY_BEAN_METHODS(
            JavaClasses classes
    ) {
        List<String> violations = classes.stream()
                .filter(javaClass ->
                        javaClass.isAnnotatedWith(Configuration.class)
                )
                .filter(javaClass ->
                        javaClass.getAnnotationOfType(Configuration.class)
                                .proxyBeanMethods()
                )
                .map(javaClass ->
                        javaClass.getName()
                                + " debe declarar "
                                + "@Configuration(proxyBeanMethods = false)"
                )
                .toList();

        assertNoViolations(
                "Configuraciones con proxyBeanMethods habilitado",
                violations
        );
    }

    /**
     * AuthApplication también debe desactivar el proxy.
     *
     * @SpringBootApplication(proxyBeanMethods = false)
     */
    @ArchTest
    static void SPRING_BOOT_APPLICATION_MUST_DISABLE_PROXY_BEAN_METHODS(
            JavaClasses classes
    ) {
        List<String> violations = classes.stream()
                .filter(javaClass ->
                        javaClass.isAnnotatedWith(SpringBootApplication.class)
                )
                .filter(javaClass ->
                        javaClass.getAnnotationOfType(SpringBootApplication.class)
                                .proxyBeanMethods()
                )
                .map(javaClass ->
                        javaClass.getName()
                                + " debe declarar "
                                + "@SpringBootApplication(proxyBeanMethods = false)"
                )
                .toList();

        assertNoViolations(
                "Bootstrap con proxyBeanMethods habilitado",
                violations
        );
    }

    /**
     * Todo método @Bean debe estar dentro de config.
     *
     * AuthApplication no debe contener métodos @Bean.
     * Input y output tampoco deben declarar configuraciones.
     */
    @ArchTest
    static void BEAN_METHODS_MUST_RESIDE_IN_CONFIG(
            JavaClasses classes
    ) {
        List<String> violations = classes.stream()
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(method -> method.isAnnotatedWith(Bean.class))
                .filter(method ->
                        !residesInPackage(
                                method.getOwner().getPackageName(),
                                CONFIG
                        )
                )
                .map(JavaMethod::getFullName)
                .toList();

        assertNoViolations(
                "Métodos @Bean fuera de pe.ask.auth.config",
                violations
        );
    }

    /**
     * Una configuración no debe crear publishers como beans.
     *
     * Son inválidos:
     *
     * @Bean Mono<User> user()
     * @Bean Flux<Event> events()
     * @Bean Publisher<?> publisher()
     *
     * Los publishers deben construirse de manera diferida
     * cuando se invoca un caso de uso o adapter.
     */
    @ArchTest
    static void BEAN_METHODS_MUST_NOT_RETURN_PUBLISHERS(
            JavaClasses classes
    ) {
        List<String> violations = classes.stream()
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(method -> method.isAnnotatedWith(Bean.class))
                .filter(method ->
                        method.getRawReturnType()
                                .isAssignableTo(Publisher.class)
                )
                .map(JavaMethod::getFullName)
                .toList();

        assertNoViolations(
                "Métodos @Bean que retornan Publisher, Mono o Flux",
                violations
        );
    }

    /**
     * Config no debe exponer como beans objetos correspondientes
     * a una petición particular.
     *
     * Se prohíben:
     *
     * - HTTP DTO.
     * - Commands.
     * - Results.
     * - Entidades de persistencia.
     */
    @ArchTest
    static void BEAN_METHODS_MUST_NOT_RETURN_REQUEST_SCOPED_MODELS(
            JavaClasses classes
    ) {
        List<String> forbiddenPackages = List.of(
                INPUT_DTO,
                PORT_IN_COMMAND,
                PORT_IN_RESULT,
                OUTPUT_ENTITY
        );

        List<String> violations = new ArrayList<>();

        classes.stream()
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(method -> method.isAnnotatedWith(Bean.class))
                .forEach(method -> {
                    String returnTypePackage =
                            method.getRawReturnType().getPackageName();

                    boolean forbidden = forbiddenPackages.stream()
                            .anyMatch(packagePattern ->
                                    residesInPackage(
                                            returnTypePackage,
                                            packagePattern
                                    )
                            );

                    if (forbidden) {
                        violations.add(method.getFullName());
                    }
                });

        assertNoViolations(
                "Métodos @Bean que retornan DTO, command, result o entity",
                violations
        );
    }

    /**
     * Las properties deben ser inmutables.
     *
     * Se establece record como estándar del proyecto.
     */
    @ArchTest
    static void CONFIGURATION_PROPERTIES_MUST_BE_RECORDS(
            JavaClasses classes
    ) {
        List<String> violations = classes.stream()
                .filter(javaClass ->
                        javaClass.isAnnotatedWith(
                                ConfigurationProperties.class
                        )
                )
                .filter(javaClass -> !javaClass.isRecord())
                .map(JavaClass::getName)
                .toList();

        assertNoViolations(
                "Configuration properties mutables",
                violations
        );
    }

    private static boolean residesInPackage(
            String packageName,
            String packagePattern
    ) {
        String prefix = packagePattern.endsWith("..")
                ? packagePattern.substring(
                0,
                packagePattern.length() - 2
        )
                : packagePattern;

        return packageName.equals(prefix)
                || packageName.startsWith(prefix + ".");
    }

    private static void assertNoViolations(
            String title,
            List<String> violations
    ) {
        if (violations.isEmpty()) {
            return;
        }

        throw new AssertionError(
                title
                        + System.lineSeparator()
                        + " - "
                        + String.join(
                        System.lineSeparator() + " - ",
                        violations
                )
        );
    }
}
