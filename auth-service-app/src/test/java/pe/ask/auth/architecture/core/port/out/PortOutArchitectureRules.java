package pe.ask.auth.architecture.core.port.out;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pe.ask.auth.architecture.core.port.ReactivePortMethodsCondition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.AllowedLibraries.JAVA;
import static pe.ask.auth.architecture.AllowedLibraries.REACTIVE_STREAMS;
import static pe.ask.auth.architecture.AllowedLibraries.REACTOR;
import static pe.ask.auth.architecture.ArchitecturePackages.MODEL;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT_PACKAGE;

public final class PortOutArchitectureRules {

    private PortOutArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Los puertos de salida deben ser interfaces.
     */
    @ArchTest
    static final ArchRule OUTPUT_PORTS_MUST_BE_INTERFACES =
            classes()
                    .that()
                    .resideInAPackage(PORT_OUT_PACKAGE)
                    .should()
                    .beInterfaces()
                    .because(
                            "los puertos de salida representan capacidades "
                                    + "requeridas por la aplicación"
                    );

    /**
     * Convención de nombrado para puertos de salida.
     * Ejemplos: UserRepositoryPort, PasswordHashPort, TokenGeneratorPort.
     */
    @ArchTest
    static final ArchRule OUTPUT_PORTS_MUST_END_WITH_OUTPUT_PORT =
            classes()
                    .that()
                    .resideInAPackage(PORT_OUT_PACKAGE)
                    .should()
                    .haveSimpleNameEndingWith("OutputPort")
                    .because(
                            "los contratos de salida deben identificarse "
                                    + "claramente mediante el sufijo OutputPort"
                    );

    /**
     * Port.out solamente puede conocer:
     * - Java estándar.
     * - Reactor.
     * - Reactive Streams.
     * - Model.
     * - Tipos propios de port.out.
     */
    @ArchTest
    static final ArchRule OUTPUT_PORTS_MUST_ONLY_DEPEND_ON_ALLOWED_TYPES =
            classes()
                    .that()
                    .resideInAPackage(PORT_OUT)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL,
                            PORT_OUT,
                            REACTOR,
                            REACTIVE_STREAMS
                    )
                    .because(
                            "port.out debe expresar capacidades abstractas "
                                    + "sin conocer sus implementaciones ni infraestructura"
                    );

    /**
     * Defensa explícita: los puertos de salida no deben depender de los puertos de entrada.
     */
    @ArchTest
    static final ArchRule OUTPUT_PORTS_MUST_NOT_DEPEND_ON_INPUT_PORTS =
            noClasses()
                    .that()
                    .resideInAPackage(PORT_OUT)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(PORT_IN)
                    .because(
                            "los puertos de salida son independientes "
                                    + "de las intenciones de entrada"
                    );

    /**
     * Todos los métodos de los puertos de salida deben ser reactivos
     * (retornar Mono, Flux o Publisher).
     */
    @ArchTest
    static final ArchRule OUTPUT_PORT_METHODS_MUST_BE_REACTIVE =
            classes()
                    .that()
                    .resideInAPackage(PORT_OUT_PACKAGE)
                    .should(new ReactivePortMethodsCondition())
                    .because(
                            "todos los contratos de salida deben mantener "
                                    + "la cadena reactiva"
                    );
}
