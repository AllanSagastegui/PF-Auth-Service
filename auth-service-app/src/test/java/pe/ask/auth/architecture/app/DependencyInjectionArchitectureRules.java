package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;

public final class DependencyInjectionArchitectureRules {

    private DependencyInjectionArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Se prohíbe @Autowired completamente.
     * Debe utilizarse:
     * - Constructor explícito.
     * - @Bean con parámetros.
     */
    @ArchTest
    static final ArchRule AUTOWIRED_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(Autowired.class.getName())
                    .because(
                            "las dependencias deben ser explícitas "
                                    + "y nunca inyectarse en campos"
                    );

    /**
     * Se prohíbe @Value.
     * La configuración debe modelarse mediante records
     * @ConfigurationProperties.
     */
    @ArchTest
    static final ArchRule VALUE_INJECTION_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(Value.class.getName())
                    .because(
                            "la configuración debe agruparse en "
                                    + "@ConfigurationProperties inmutables"
                    );

    @ArchTest
    static final ArchRule JAKARTA_INJECT_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("jakarta.inject.Inject")
                    .because(
                            "la aplicación utiliza composición explícita"
                    );

    @ArchTest
    static final ArchRule JAKARTA_RESOURCE_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("jakarta.annotation.Resource")
                    .because(
                            "la aplicación utiliza composición explícita"
                    );
}
