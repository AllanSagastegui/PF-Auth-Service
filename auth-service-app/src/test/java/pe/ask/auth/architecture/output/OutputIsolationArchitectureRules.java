package pe.ask.auth.architecture.output;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_DATABASE;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_KAFKA;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PERSISTENCE;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PRODUCER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_SECURITY;

public final class OutputIsolationArchitectureRules {

    private OutputIsolationArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchRule PERSISTENCE_MUST_NOT_DEPEND_ON_OTHER_OUTPUT_ADAPTERS =
            noClasses()
                    .that()
                    .resideInAnyPackage(OUTPUT_PERSISTENCE, OUTPUT_DATABASE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER,
                            OUTPUT_SECURITY
                    )
                    .because(
                            "database/persistence debe implementar únicamente "
                                    + "capacidades de persistencia"
                    );

    @ArchTest
    static final ArchRule KAFKA_MUST_NOT_DEPEND_ON_OTHER_OUTPUT_ADAPTERS =
            noClasses()
                    .that()
                    .resideInAnyPackage(OUTPUT_KAFKA, OUTPUT_PRODUCER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE,
                            OUTPUT_SECURITY
                    )
                    .because(
                            "el producer/kafka debe publicar mensajes sin orquestar "
                                    + "persistencia o seguridad"
                    );

    @ArchTest
    static final ArchRule SECURITY_MUST_NOT_DEPEND_ON_OTHER_OUTPUT_ADAPTERS =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE,
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER
                    )
                    .because(
                            "security debe implementar capacidades criptográficas "
                                    + "sin acceder a persistence o messaging"
                    );
}
