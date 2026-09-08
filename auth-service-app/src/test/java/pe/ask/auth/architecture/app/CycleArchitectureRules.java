package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static pe.ask.auth.architecture.ArchitecturePackages.ROOT;

public final class CycleArchitectureRules {

    private CycleArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Comprueba ciclos entre:
     * core
     * input
     * output
     * config
     * Ejemplo inválido:
     * input -> core -> output -> input
     * Ejemplo inválido:
     * config -> input -> config
     */
    @ArchTest
    static final ArchRule TOP_LEVEL_PACKAGES_MUST_BE_FREE_OF_CYCLES =
            slices()
                    .matching(ROOT + ".(*)..")
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "los módulos principales no deben formar "
                                    + "dependencias cíclicas"
                    );

    /**
     * Comprueba ciclos entre subpaquetes de configuración.
     * Ejemplo inválido:
     * config.input -> config.output -> config.input
     */
    @ArchTest
    static final ArchRule CONFIG_PACKAGES_MUST_BE_FREE_OF_CYCLES =
            slices()
                    .matching(ROOT + ".config.(*)..")
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "las configuraciones deben poder construirse "
                                    + "sin dependencias circulares"
                    )
                    .allowEmptyShould(true);
}