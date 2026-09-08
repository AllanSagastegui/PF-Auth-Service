package pe.ask.auth.architecture.input.api;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

public class InputApiRules {
    private InputApiRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchTests INPUT_ARCHITECTURE =
            ArchTests.in(InputArchitectureRules.class);

    @ArchTest
    static final ArchTests INPUT_API_BOUNDARY_ARCHITECTURE =
            ArchTests.in(InputApiBoundaryArchitectureRules.class);
}
