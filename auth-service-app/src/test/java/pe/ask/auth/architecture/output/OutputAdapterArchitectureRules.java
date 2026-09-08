package pe.ask.auth.architecture.output;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_DATABASE_ADAPTER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_KAFKA_ADAPTER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PERSISTENCE_ADAPTER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_PRODUCER_ADAPTER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_SECURITY_ADAPTER;

public final class OutputAdapterArchitectureRules {

    private OutputAdapterArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_MUST_END_WITH_ADAPTER =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_SECURITY_ADAPTER
                    )
                    .should()
                    .haveSimpleNameEndingWith("Adapter")
                    .because(
                            "las implementaciones de port.out deben "
                                    + "identificarse explícitamente como adapters"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_SECURITY_ADAPTER
                    )
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "los adapters deben ser componentes cerrados "
                                    + "sin jerarquías de herencia"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_MUST_IMPLEMENT_ONE_OUTPUT_PORT =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_SECURITY_ADAPTER
                    )
                    .should(
                            new ImplementExactlyOneOutputPortCondition()
                    )
                    .because(
                            "cada adapter debe representar exactamente "
                                    + "una capacidad de port.out"
                    )
                    .allowEmptyShould(true);

    /**
     * Como app/config es nuestro composition root,
     * los adapters no se auto-registran con component scan.
     */
    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_MUST_NOT_BE_COMPONENTS =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_SECURITY_ADAPTER
                    )
                    .should()
                    .beAnnotatedWith(Component.class)
                    .because(
                            "los adapters deben ser construidos explícitamente "
                                    + "desde app/config"
                    );

    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_MUST_NOT_BE_SERVICES =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_SECURITY_ADAPTER
                    )
                    .should()
                    .beAnnotatedWith(Service.class)
                    .because(
                            "las implementaciones de infraestructura "
                                    + "no son servicios de aplicación"
                    );
}
