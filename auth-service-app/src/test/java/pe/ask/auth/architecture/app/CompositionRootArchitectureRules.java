package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.CORE;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_DTO;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT_ENTITY;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_COMMAND;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_RESULT;

public final class CompositionRootArchitectureRules {

    private CompositionRootArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Core, input y output nunca deben importar config.
     * La dirección válida es:
     * config -> core/input/output
     * Nunca:
     * core/input/output -> config
     */
    @ArchTest
    static final ArchRule MODULES_MUST_NOT_DEPEND_ON_APP_CONFIG =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            CORE,
                            INPUT,
                            OUTPUT
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(CONFIG)
                    .because(
                            "el módulo app es la capa más externa "
                                    + "y ningún módulo puede depender de ella"
                    );

    /**
     * Config no debe conocer objetos correspondientes a una
     * petición HTTP o registro de persistencia.
     */
    @ArchTest
    static final ArchRule CONFIG_MUST_NOT_DEPEND_ON_INTERNAL_REPRESENTATIONS =
            noClasses()
                    .that()
                    .resideInAPackage(CONFIG)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            INPUT_DTO,
                            PORT_IN_COMMAND,
                            PORT_IN_RESULT,
                            OUTPUT_ENTITY
                    )
                    .because(
                            "config debe conectar componentes y no manipular "
                                    + "DTO, commands, results o entities"
                    );

    /**
     * Config no puede contener clases que pertenezcan
     * conceptualmente a otra capa.
     */
    @ArchTest
    static final ArchRule CONFIG_MUST_NOT_DECLARE_APPLICATION_ROLES =
            noClasses()
                    .that()
                    .resideInAPackage(CONFIG)
                    .should()
                    .haveNameMatching(
                            ".*(Handler|Router|Controller|Advice|Adapter|"
                                    + "Repository|UseCase|Service|Mapper|"
                                    + "Entity|Document|Request|Response|"
                                    + "Command|Result)$"
                    )
                    .because(
                            "el paquete config solamente debe contener "
                                    + "configuraciones y properties"
                    );

    /**
     * Config puede construir componentes, pero no ejecutar:
     *
     * - Port.in.
     * - Port.out.
     * - Casos de uso concretos.
     *
     * Invocar un constructor está permitido.
     * Invocar register(), login(), save(), find(), etc. no.
     */
    @ArchTest
    static final ArchRule CONFIG_MUST_NOT_EXECUTE_BUSINESS_OR_PORT_METHODS =
            classes()
                    .that()
                    .resideInAPackage(CONFIG)
                    .should(
                            NoMethodCallsToPackagesCondition
                                    .notCallMethodsIn(
                                    )
                    )
                    .because(
                            "la configuración debe construir y conectar objetos, "
                                    + "pero no ejecutar operaciones del negocio"
                    );

    /**
     * Ninguna clase productiva debe obtener beans manualmente
     * desde ApplicationContext o BeanFactory.
     */
    @ArchTest
    static final ArchRule APPLICATION_MUST_NOT_USE_SERVICE_LOCATOR =
            classes()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should(new NoServiceLocatorCallsCondition())
                    .because(
                            "las dependencias deben declararse explícitamente "
                                    + "mediante constructores o métodos @Bean"
                    );
}
