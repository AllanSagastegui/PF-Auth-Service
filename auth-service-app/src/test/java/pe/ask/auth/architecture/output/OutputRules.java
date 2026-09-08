package pe.ask.auth.architecture.output;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import pe.ask.auth.architecture.output.database.DatabaseArchitectureRules;
import pe.ask.auth.architecture.output.producer.ProducerArchitectureRules;
import pe.ask.auth.architecture.output.security.SecurityArchitectureRules;

public final class OutputRules {

    private OutputRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchTests BOUNDARY_RULES =
            ArchTests.in(
                    OutputBoundaryArchitectureRules.class
            );

    @ArchTest
    static final ArchTests OUTPUT_RULES =
            ArchTests.in(
                    OutputArchitectureRules.class
            );

    @ArchTest
    static final ArchTests ADAPTER_RULES =
            ArchTests.in(
                    OutputAdapterArchitectureRules.class
            );

    @ArchTest
    static final ArchTests ISOLATION_RULES =
            ArchTests.in(
                    OutputIsolationArchitectureRules.class
            );

    @ArchTest
    static final ArchTests DATABASE_RULES =
            ArchTests.in(
                    DatabaseArchitectureRules.class
            );

    @ArchTest
    static final ArchTests PRODUCER_RULES =
            ArchTests.in(
                    ProducerArchitectureRules.class
            );

    @ArchTest
    static final ArchTests SECURITY_RULES =
            ArchTests.in(
                    SecurityArchitectureRules.class
            );
}
