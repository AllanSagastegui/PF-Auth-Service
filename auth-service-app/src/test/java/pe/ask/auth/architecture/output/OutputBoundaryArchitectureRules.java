package pe.ask.auth.architecture.output;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_DATABASE;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_KAFKA;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_MESSAGING;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PERSISTENCE;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PRODUCER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_SECURITY;

public final class OutputBoundaryArchitectureRules {

    private OutputBoundaryArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Los únicos tipos de adapters de salida permitidos son:
     * - database / persistence
     * - producer / messaging / kafka
     * - security
     */
    @ArchTest
    static final ArchRule OUTPUT_MUST_ONLY_CONTAIN_ALLOWED_ADAPTERS =
            classes()
                    .that()
                    .resideInAPackage(OUTPUT)
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE,
                            OUTPUT_MESSAGING,
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER,
                            OUTPUT_SECURITY
                    )
                    .because(
                            "output solamente puede contener adapters "
                                    + "de database, producer/kafka y security"
                    );
}
