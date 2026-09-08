package pe.ask.auth.architecture.output;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN;
import static pe.ask.auth.architecture.ArchitecturePackages.USE_CASE;

public final class OutputArchitectureRules {

    private OutputArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Output nunca puede conocer el lado de entrada.
     *
     * Prohibido:
     *
     * output -> input
     * output -> port.in
     * output -> usecase
     * output -> config
     *
     * Permitido:
     *
     * output -> port.out
     * output -> model
     */
    @ArchTest
    static final ArchRule OUTPUT_MUST_NOT_DEPEND_ON_INPUT_SIDE =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            PORT_IN,
                            USE_CASE,
                            INPUT,
                            CONFIG
                    )
                    .because(
                            "los adapters de salida implementan capacidades "
                                    + "de port.out y no deben conocer "
                                    + "protocolos de entrada ni casos de uso"
                    );

    /**
     * Output tampoco puede depender de HTTP/WebFlux Server.
     *
     * Un adapter de salida no tiene por qué conocer:
     *
     * ServerRequest
     * ServerResponse
     * RouterFunction
     * WebFilter
     */
    @ArchTest
    static final ArchRule OUTPUT_MUST_NOT_DEPEND_ON_WEB_SERVER_API =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(
                            "org.springframework.web.reactive.function.server.."
                    )
                    .because(
                            "HTTP pertenece exclusivamente al adapter input.api"
                    );
}
