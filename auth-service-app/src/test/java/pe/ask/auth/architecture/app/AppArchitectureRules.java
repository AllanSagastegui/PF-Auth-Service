package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

public final class AppArchitectureRules {

    private AppArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    public static final ArchTests HEXAGONAL_APPLICATION_RULES =
            ArchTests.in(HexagonalApplicationArchitectureRules.class);

    @ArchTest
    public static final ArchTests BOOTSTRAP_RULES =
            ArchTests.in(BootstrapArchitectureRules.class);

    @ArchTest
    public static final ArchTests SPRING_CONFIGURATION_RULES =
            ArchTests.in(SpringConfigurationArchitectureRules.class);

    @ArchTest
    public static final ArchTests COMPOSITION_ROOT_RULES =
            ArchTests.in(CompositionRootArchitectureRules.class);

    @ArchTest
    public static final ArchTests DEPENDENCY_INJECTION_RULES =
            ArchTests.in(DependencyInjectionArchitectureRules.class);

    @ArchTest
    public static final ArchTests LIFECYCLE_RULES =
            ArchTests.in(LifecycleArchitectureRules.class);

    @ArchTest
    public static final ArchTests REACTIVE_TECHNOLOGY_RULES =
            ArchTests.in(ReactiveTechnologyArchitectureRules.class);

    @ArchTest
    public static final ArchTests REACTIVE_CALL_RULES =
            ArchTests.in(ReactiveCallArchitectureRules.class);

    @ArchTest
    public static final ArchTests CYCLE_RULES =
            ArchTests.in(CycleArchitectureRules.class);
}