package pe.ask.auth.architecture.core.port.in;

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
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_PACKAGE;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT;

public final class PortInArchitectureRules {

    private PortInArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Las clases ubicadas directamente en port.in deben ser interfaces.
     */
    @ArchTest
    static final ArchRule INPUT_PORTS_MUST_BE_INTERFACES =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_PACKAGE)
                    .should()
                    .beInterfaces()
                    .because(
                            "los puertos de entrada representan contratos "
                                    + "ejecutables del negocio"
                    );

    /**
     * Convención de nombrado para puertos de entrada.
     * Ejemplos: LoginInputPort, RegisterUserInputPort.
     */
    @ArchTest
    static final ArchRule INPUT_PORTS_MUST_END_WITH_INPUT_PORT =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_PACKAGE)
                    .should()
                    .haveSimpleNameEndingWith("InputPort")
                    .because(
                            "los contratos de entrada deben identificarse "
                                    + "claramente mediante el sufijo InputPort"
                    );

    /**
     * Port.in solamente puede conocer:
     * - Java estándar.
     * - Reactor.
     * - Reactive Streams.
     * - Model.
     * - Tipos de port.in (commands, results, otros puertos de entrada).
     */
    @ArchTest
    static final ArchRule INPUT_PORTS_MUST_ONLY_DEPEND_ON_ALLOWED_TYPES =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL,
                            PORT_IN,
                            REACTOR,
                            REACTIVE_STREAMS
                    )
                    .because(
                            "port.in no debe conocer port.out, use cases "
                                    + "ni detalles de infraestructura"
                    );

    /**
     * Defensa explícita: los puertos de entrada no deben conocer los de salida.
     */
    @ArchTest
    static final ArchRule INPUT_PORTS_MUST_NOT_DEPEND_ON_OUTPUT_PORTS =
            noClasses()
                    .that()
                    .resideInAPackage(PORT_IN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(PORT_OUT)
                    .because(
                            "un puerto de entrada no debe conocer "
                                    + "capacidades internas requeridas por el use case"
                    );

    /**
     * Todos los métodos de los puertos de entrada deben ser reactivos
     * (retornar Mono, Flux o Publisher).
     */
    @ArchTest
    static final ArchRule INPUT_PORT_METHODS_MUST_BE_REACTIVE =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_PACKAGE)
                    .should(new ReactivePortMethodsCondition())
                    .because(
                            "todos los contratos de entrada deben mantener "
                                    + "la cadena reactiva"
                    );
}
