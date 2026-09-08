package pe.ask.auth.architecture.core.usecase;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pe.ask.auth.core.usecase.annotation.UseCase;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.AllowedLibraries.*;
import static pe.ask.auth.architecture.ArchitecturePackages.*;

public final class UseCaseArchitectureRules {

    private UseCaseArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Toda implementación ubicada directamente en core.usecase
     * debe estar marcada con @UseCase.
     *
     * Esto corrige tu regla anterior:
     *
     * classes().should().beAnnotatedWith(...)
     *
     * que terminaba aplicándose a todas las clases importadas.
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_BE_ANNOTATED =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should()
                    .beAnnotatedWith(UseCase.class)
                    .because(
                            "las implementaciones de los casos de uso "
                                    + "deben identificarse explícitamente"
                    );

    /**
     * @UseCase solamente puede utilizarse dentro de core.usecase.
     */
    @ArchTest
    static final ArchRule USE_CASE_ANNOTATION_MUST_ONLY_BE_USED_ON_USE_CASES =
            classes()
                    .that()
                    .areAnnotatedWith(UseCase.class)
                    .should()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .because(
                            "@UseCase identifica implementaciones concretas "
                                    + "de puertos de entrada"
                    );

    /**
     * Implementaciones cerradas.
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "los casos de uso no forman jerarquías "
                                    + "de herencia"
                    );

    /**
     * Deben ser públicas porque app/config debe poder construirlas.
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_BE_PUBLIC =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should()
                    .haveModifier(JavaModifier.PUBLIC)
                    .because(
                            "el composition root debe poder instanciar "
                                    + "los casos de uso"
                    );

    /**
     * Cada implementación concreta debe implementar exactamente
     * un port.in.
     *
     * Esto fuerza:
     *
     * LoginService -> LoginUseCase
     *
     * y evita:
     *
     * AuthService
     *   implements LoginUseCase,
     *              RegisterUseCase,
     *              RefreshTokenUseCase,
     *              ForgotPasswordUseCase...
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_IMPLEMENT_EXACTLY_ONE_INPUT_PORT =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should(
                            new ImplementExactlyOneInputPortCondition()
                    )
                    .because(
                            "cada caso de uso debe tener una única "
                                    + "responsabilidad de entrada"
                    );

    /**
     * Un use case puede depender únicamente de:
     *
     * model
     * port.in
     * port.out
     * otros tipos usecase
     * Reactor
     * Java
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_ONLY_DEPEND_ON_CORE_CONTRACTS =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            JAVA,
                            MODEL,
                            PORT_IN,
                            PORT_OUT,
                            USE_CASE,
                            REACTOR,
                            REACTIVE_STREAMS
                    )
                    .because(
                            "los casos de uso solamente deben orquestar "
                                    + "dominio y puertos"
                    );

    /**
     * Los adapters no pueden conocer las implementaciones
     * concretas del caso de uso.
     *
     * Handler:
     *
     * LoginUseCase
     * LoginService
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_ONLY_BE_DEPENDED_ON_BY_CONFIG =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should()
                    .onlyHaveDependentClassesThat()
                    .resideInAnyPackage(
                            USE_CASE,
                            CONFIG
                    )
                    .because(
                            "los adapters deben depender de port.in "
                                    + "y nunca de implementaciones concretas"
                    );

    /**
     * Protección explícita adicional.
     */
    @ArchTest
    static final ArchRule USE_CASES_MUST_NOT_DEPEND_ON_OUTER_LAYERS =
            noClasses()
                    .that()
                    .resideInAPackage(USE_CASE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            INPUT,
                            OUTPUT,
                            CONFIG
                    )
                    .because(
                            "un caso de uso debe ser completamente "
                                    + "independiente de los adapters"
                    );

    /**
     * Cualquier API pública del use case debe conservar
     * el modelo reactivo.
     *
     * Los helpers privados pueden ser síncronos porque pueden
     * realizar cálculo puro en memoria.
     */
    @ArchTest
    static final ArchRule PUBLIC_USE_CASE_METHODS_MUST_BE_REACTIVE =
            classes()
                    .that()
                    .resideInAPackage(USE_CASE_PACKAGE)
                    .should(
                            new PublicMethodsMustBeReactiveCondition()
                    )
                    .because(
                            "ninguna API pública de un caso de uso "
                                    + "debe romper la cadena reactiva"
                    );
}
