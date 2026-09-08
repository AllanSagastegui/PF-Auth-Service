package pe.ask.auth.architecture.core.model;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.AllowedLibraries.JAVA;
import static pe.ask.auth.architecture.ArchitecturePackages.MODEL;

public final class ModelArchitectureRules {

    private ModelArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Model es la zona más pura de toda la aplicación.
     *
     * Puede utilizar únicamente:
     *
     * - Java estándar.
     * - Otros tipos del propio model.
     *
     * Ni siquiera Reactor está permitido aquí.
     */
    @ArchTest
    static final ArchRule MODEL_MUST_ONLY_DEPEND_ON_DOMAIN_TYPES =
            classes()
                    .that()
                    .resideInAPackage(MODEL)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL
                    )
                    .because(
                            "el modelo de dominio debe utilizar exclusivamente "
                                    + "Java y tipos propios del dominio"
                    );

    /**
     * Una clase de dominio no debe llamarse como una representación
     * de HTTP, persistencia o aplicación.
     */
    @ArchTest
    static final ArchRule MODEL_MUST_NOT_USE_TECHNICAL_NAMES =
            noClasses()
                    .that()
                    .resideInAPackage(MODEL)
                    .should()
                    .haveNameMatching(
                            ".*(Entity|Document|Dto|DTO|Request|Response|"
                                    + "Command|Result|Repository|Adapter|"
                                    + "Controller|Handler|Router)$"
                    )
                    .because(
                            "los nombres del modelo deben representar "
                                    + "conceptos del dominio y no tecnologías"
                    );

    /**
     * Validación adicional de inmutabilidad.
     */
    @ArchTest
    static final ArchRule DOMAIN_MODELS_MUST_BE_IMMUTABLE =
            classes()
                    .that()
                    .resideInAPackage(MODEL)
                    .should(new ImmutableDomainModelCondition())
                    .because(
                            "los objetos del dominio deben ser inmutables "
                                    + "y seguros para composición reactiva"
                    );
}
