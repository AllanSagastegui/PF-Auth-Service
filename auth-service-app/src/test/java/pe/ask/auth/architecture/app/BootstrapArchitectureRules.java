package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.CORE;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.ROOT;

public final class BootstrapArchitectureRules {

    private BootstrapArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * MainApplication debe:
     * - Estar directamente en pe.ask.auth.
     * - Tener un nombre único y reconocible.
     * - Ser final.
     */
    @ArchTest
    static final ArchRule SPRING_BOOT_APPLICATION_MUST_HAVE_CORRECT_LOCATION =
            classes()
                    .that()
                    .areAnnotatedWith(SpringBootApplication.class)
                    .should()
                    .resideInAPackage(ROOT)
                    .andShould()
                    .haveSimpleName("MainApplication")
                    .andShould()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "MainApplication debe ser el único punto "
                                    + "de arranque del microservicio"
                    );

    /**
     * El bootstrap no debe importar casos de uso, handlers,
     * routers, adapters o configuraciones concretas.
     *
     * Spring encontrará las configuraciones mediante scanning.
     */
    @ArchTest
    static final ArchRule BOOTSTRAP_MUST_NOT_DEPEND_ON_APPLICATION_LAYERS =
            noClasses()
                    .that()
                    .areAnnotatedWith(SpringBootApplication.class)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            CORE,
                            INPUT,
                            OUTPUT,
                            CONFIG
                    )
                    .because(
                            "el bootstrap solamente debe iniciar Spring Boot "
                                    + "y no ejecutar ni conocer componentes "
                                    + "concretos de la aplicación"
                    );

    /**
     * Obliga a utilizar:
     *
     * application.setWebApplicationType(WebApplicationType.REACTIVE)
     *
     * La comprobación del valor REACTIVE se complementará
     * con una prueba de contexto.
     */
    @ArchTest
    static final ArchRule BOOTSTRAP_MUST_EXPLICITLY_SET_WEB_APPLICATION_TYPE =
            classes()
                    .that()
                    .areAnnotatedWith(SpringBootApplication.class)
                    .should()
                    .callMethod(
                            SpringApplication.class,
                            "setWebApplicationType",
                            WebApplicationType.class
                    )
                    .because(
                            "la aplicación debe forzar explícitamente "
                                    + "WebApplicationType.REACTIVE"
                    );

    /**
     * Se conserva la auto-configuración de Spring Boot.
     *
     * Para personalizaciones se utiliza WebFluxConfigurer,
     * pero no @EnableWebFlux.
     */
    @ArchTest
    static final ArchRule ENABLE_WEBFLUX_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAnnotatedWith("org.springframework.web.reactive.config.EnableWebFlux")
                    .because(
                            "Spring Boot debe conservar el control de su "
                                    + "auto-configuración WebFlux"
                    );

    /**
     * Debe existir exactamente una clase @SpringBootApplication.
     */
    @ArchTest
    static void THERE_MUST_BE_EXACTLY_ONE_SPRING_BOOT_APPLICATION(
            JavaClasses classes
    ) {
        List<JavaClass> applications = classes.stream()
                .filter(javaClass ->
                        javaClass.isAnnotatedWith(SpringBootApplication.class)
                )
                .toList();

        if (applications.size() != 1) {
            String foundApplications = applications.stream()
                    .map(JavaClass::getName)
                    .sorted()
                    .toList()
                    .toString();

            throw new AssertionError(
                    "Debe existir exactamente una clase @SpringBootApplication. "
                            + "Se encontraron "
                            + applications.size()
                            + ": "
                            + foundApplications
            );
        }
    }

    /**
     * Debe existir un único método main público y estático,
     * perteneciente a MainApplication.
     */
    @ArchTest
    static void THERE_MUST_BE_EXACTLY_ONE_MAIN_METHOD(
            JavaClasses classes
    ) {
        List<JavaMethod> mainMethods = classes.stream()
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(method -> method.getName().equals("main"))
                .filter(method ->
                        method.getModifiers().contains(JavaModifier.PUBLIC)
                )
                .filter(method ->
                        method.getModifiers().contains(JavaModifier.STATIC)
                )
                .toList();

        if (mainMethods.size() != 1) {
            throw new AssertionError(
                    "Debe existir exactamente un método public static main. "
                            + "Se encontraron: "
                            + mainMethods
            );
        }

        JavaMethod mainMethod = mainMethods.getFirst();

        if (!mainMethod.getOwner()
                .isAnnotatedWith(SpringBootApplication.class)) {
            throw new AssertionError(
                    "El método main debe pertenecer a MainApplication, "
                            + "pero fue encontrado en "
                            + mainMethod.getOwner().getName()
            );
        }
    }
}
