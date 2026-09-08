package pe.ask.auth.architecture.core;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.AllowedLibraries.*;
import static pe.ask.auth.architecture.ArchitecturePackages.*;

public final class CoreArchitectureRules {

    private CoreArchitectureRules() {
    }

    /**
     * Core solamente puede depender de:
     * - Java estándar.
     * - Clases pertenecientes al propio core.
     * - Project Reactor.
     * - Reactive Streams.
     * No se permite:
     * - Spring.
     * - SLF4J.
     * - Jackson.
     * - Jakarta Validation.
     * - R2DBC.
     * - Redis.
     * - MongoDB.
     * - Azure SDK.
     * - WebFlux.
     * - Adapters.
     */
    @ArchTest
    static final ArchRule CORE_MUST_ONLY_DEPEND_ON_ALLOWED_PACKAGES =
            classes()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            CORE,
                            REACTOR,
                            REACTIVE_STREAMS
                    )
                    .because(
                            "el core debe permanecer independiente de Spring, "
                                    + "protocolos de entrada y tecnologías de infraestructura"
                    );

    /**
     * Dentro de core solamente pueden existir las cuatro
     * responsabilidades permitidas:
     * - model
     * - port.in
     * - port.out
     * - usecase
     */
    @ArchTest
    static final ArchRule CORE_MUST_ONLY_CONTAIN_ALLOWED_AREAS =
            classes()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .resideInAnyPackage(
                            MODEL,
                            PORT_IN,
                            PORT_OUT,
                            USE_CASE
                    )
                    .because(
                            "el core solamente debe contener model, "
                                    + "port.in, port.out y usecase"
                    );

    /**
     * Protección explícita de la dirección hexagonal.
     */
    @ArchTest
    static final ArchRule CORE_MUST_NOT_DEPEND_ON_OUTER_LAYERS =
            noClasses()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            INPUT,
                            OUTPUT,
                            CONFIG
                    )
                    .because(
                            "las dependencias deben apuntar hacia el core "
                                    + "y nunca desde el core hacia adapters o config"
                    );

    /**
     * Aunque java.. está permitido en general, estas APIs del JDK
     * representan infraestructura, bloqueo, concurrencia externa
     * o efectos secundarios.
     */
    @ArchTest
    static final ArchRule CORE_MUST_NOT_USE_TECHNICAL_JDK_APIS =
            noClasses()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "java.io..",
                            "java.nio.file..",
                            "java.net..",
                            "java.sql..",
                            "javax.sql..",
                            "java.util.concurrent..",
                            "java.lang.reflect..",
                            "java.lang.management..",
                            "javax.crypto.."
                    )
                    .because(
                            "el core debe contener lógica determinística "
                                    + "y composición reactiva, no infraestructura"
                    );

    /**
     * La criptografía concreta tampoco pertenece al core.
     * El core debe solicitar capacidades como:
     * PasswordHashPort
     * TokenSignerPort
     * y dejar su implementación al output adapter.
     */
    @ArchTest
    static final ArchRule CORE_MUST_NOT_IMPLEMENT_CRYPTOGRAPHIC_INFRASTRUCTURE =
            noClasses()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("java.security..")
                    .because(
                            "los detalles criptográficos deben implementarse "
                                    + "mediante adapters de salida"
                    );

    /**
     * El core no controla threads ni schedulers.
     *
     * Si un adapter necesita aislar CPU-intensive work,
     * esa decisión pertenece al adapter/app.
     */
    @ArchTest
    static final ArchRule CORE_MUST_NOT_DEPEND_ON_REACTOR_SCHEDULERS =
            noClasses()
                    .that()
                    .resideInAPackage(CORE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(
                            "reactor.core.scheduler.."
                    )
                    .because(
                            "el core debe describir composición reactiva "
                                    + "y no decisiones de threading"
                    );

    @ArchTest
    static final ArchRule CORE_MUST_NOT_ESCAPE_REACTIVE_PIPELINE =
            classes()
                    .that()
                    .resideInAPackage(CORE)
                    .should(
                            new CoreReactiveCallsCondition()
                    )
                    .because(
                            "el core nunca debe bloquear, subscribirse "
                                    + "manualmente ni convertir Publishers "
                                    + "a estructuras imperativas"
                    );
}
