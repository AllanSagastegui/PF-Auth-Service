package pe.ask.auth.architecture.input.api;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API;

public final class InputApiBoundaryArchitectureRules {

    private InputApiBoundaryArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * El único adapter de entrada permitido es API.
     * Permitido:
     * pe.ask.auth.input.api..
     * Prohibido:
     * pe.ask.auth.input.kafka..
     * pe.ask.auth.input.grpc..
     * pe.ask.auth.input.scheduler..
     * pe.ask.auth.input.cli..
     * pe.ask.auth.input.servicebus..
     * También se prohíben clases directamente dentro de:
     * pe.ask.auth.input
     */
    @ArchTest
    static final ArchRule INPUT_MUST_CONTAIN_ONLY_API_ADAPTER =
            classes()
                    .that()
                    .resideInAPackage(INPUT)
                    .should()
                    .resideInAPackage(INPUT_API)
                    .because(
                            "API es el único adapter de entrada permitido "
                                    + "en este microservicio"
                    );
}
