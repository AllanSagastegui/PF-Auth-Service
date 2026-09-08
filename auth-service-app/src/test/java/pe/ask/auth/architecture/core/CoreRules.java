package pe.ask.auth.architecture.core;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import pe.ask.auth.architecture.core.model.ModelArchitectureRules;
import pe.ask.auth.architecture.core.port.in.PortInArchitectureRules;
import pe.ask.auth.architecture.core.port.in.PortInDataArchitectureRules;
import pe.ask.auth.architecture.core.port.out.PortOutArchitectureRules;
import pe.ask.auth.architecture.core.usecase.UseCaseArchitectureRules;

public final class CoreRules {

    private CoreRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchTests CORE_ARCHITECTURE =
            ArchTests.in(CoreArchitectureRules.class);

    @ArchTest
    static final ArchTests MODEL_ARCHITECTURE =
            ArchTests.in(ModelArchitectureRules.class);

    @ArchTest
    static final ArchTests PORT_IN_ARCHITECTURE =
            ArchTests.in(PortInArchitectureRules.class);

    @ArchTest
    static final ArchTests PORT_IN_DATA_ARCHITECTURE =
            ArchTests.in(PortInDataArchitectureRules.class);

    @ArchTest
    static final ArchTests PORT_OUT_ARCHITECTURE =
            ArchTests.in(PortOutArchitectureRules.class);

    @ArchTest
    static final ArchTests USE_CASE_ARCHITECTURE =
            ArchTests.in(UseCaseArchitectureRules.class);

    @ArchTest
    static final ArchTests DETERMINISM_ARCHITECTURE =
            ArchTests.in(CoreDeterminismArchitectureRules.class);

    @ArchTest
    static final ArchTests CYCLE_ARCHITECTURE =
            ArchTests.in(CoreCycleArchitectureRules.class);
}
