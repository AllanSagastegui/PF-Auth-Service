package pe.ask.auth.architecture.core;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static pe.ask.auth.architecture.ArchitecturePackages.CORE;

public final class CoreDeterminismArchitectureRules {

    private CoreDeterminismArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchRule CORE_MUST_NOT_USE_HIDDEN_NON_DETERMINISM =
            classes()
                    .that()
                    .resideInAPackage(CORE)
                    .should(
                            new CoreDeterminismCondition()
                    )
                    .because(
                            "tiempo, identificadores aleatorios y environment "
                                    + "deben ser dependencias explícitas"
                    );
}
