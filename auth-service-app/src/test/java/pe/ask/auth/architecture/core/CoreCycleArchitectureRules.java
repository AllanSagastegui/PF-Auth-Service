package pe.ask.auth.architecture.core;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static pe.ask.auth.architecture.ArchitecturePackages.ROOT;

public final class CoreCycleArchitectureRules {

    private CoreCycleArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Detecta ciclos:
     * model -> port -> usecase -> model
     */
    @ArchTest
    static final ArchRule CORE_PACKAGES_MUST_BE_FREE_OF_CYCLES =
            slices()
                    .matching(
                            ROOT + ".core.(*).."
                    )
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "las responsabilidades internas del core "
                                    + "no deben formar ciclos"
                    );

    /**
     * Detecta específicamente:
     * port.in -> port.out -> port.in
     */
    @ArchTest
    static final ArchRule PORT_PACKAGES_MUST_BE_FREE_OF_CYCLES =
            slices()
                    .matching(
                            ROOT + ".core.port.(*).."
                    )
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "port.in y port.out deben ser contratos "
                                    + "independientes"
                    );
}
