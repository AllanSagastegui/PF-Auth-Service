package pe.ask.auth.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import pe.ask.auth.architecture.app.AppArchitectureRules;
import pe.ask.auth.architecture.core.CoreRules;
import pe.ask.auth.architecture.input.InputRules;
import pe.ask.auth.architecture.output.OutputRules;

@AnalyzeClasses(
        packages = ArchitecturePackages.ROOT,
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchTests APP_RULES =
            ArchTests.in(AppArchitectureRules.class);

    @ArchTest
    static final ArchTests CORE_RULES =
            ArchTests.in(CoreRules.class);

    @ArchTest
    static final ArchTests INPUT_RULES =
            ArchTests.in(InputRules.class);

    @ArchTest
    static final ArchTests OUTPUT_RULES =
            ArchTests.in(OutputRules.class);
}
