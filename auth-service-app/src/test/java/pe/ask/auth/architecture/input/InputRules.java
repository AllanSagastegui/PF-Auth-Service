package pe.ask.auth.architecture.input;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import pe.ask.auth.architecture.input.api.InputApiRules;

public final class InputRules {

    private InputRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchTests INPUT_API_RULES =
            ArchTests.in(InputApiRules.class);
}
