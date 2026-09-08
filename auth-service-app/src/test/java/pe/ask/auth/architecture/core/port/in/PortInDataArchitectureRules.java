package pe.ask.auth.architecture.core.port.in;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static pe.ask.auth.architecture.AllowedLibraries.JAVA;
import static pe.ask.auth.architecture.ArchitecturePackages.MODEL;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_COMMAND;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_RESULT;

public final class PortInDataArchitectureRules {

    private PortInDataArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchRule COMMANDS_MUST_ONLY_DEPEND_ON_JAVA_AND_MODEL =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_COMMAND)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL,
                            PORT_IN_COMMAND
                    )
                    .because(
                            "los commands son datos de entrada del negocio "
                                    + "y no deben contener Reactor ni infraestructura"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule RESULTS_MUST_ONLY_DEPEND_ON_JAVA_AND_MODEL =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_RESULT)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL,
                            PORT_IN_RESULT
                    )
                    .because(
                            "los results son datos de salida del negocio "
                                    + "y no deben contener Reactor ni infraestructura"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule COMMANDS_MUST_BE_RECORDS =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_COMMAND)
                    .should(new BeRecordsCondition())
                    .because(
                            "los commands deben ser estructuras "
                                    + "inmutables de datos"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule RESULTS_MUST_BE_RECORDS =
            classes()
                    .that()
                    .resideInAPackage(PORT_IN_RESULT)
                    .should(new BeRecordsCondition())
                    .because(
                            "los results deben ser estructuras "
                                    + "inmutables de datos"
                    )
                    .allowEmptyShould(true);
}
