package pe.ask.auth.architecture.input.api;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_DTO;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_ERROR;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_FILTER;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_HANDLER;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_MAPPER;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT_API_ROUTER;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT;
import static pe.ask.auth.architecture.ArchitecturePackages.USE_CASE;

public final class InputArchitectureRules {

    private static final String SPRING_WEB_ANNOTATIONS =
            "org.springframework.web.bind.annotation..";

    private static final String WEBFLUX_FUNCTIONAL_SERVER =
            "org.springframework.web.reactive.function.server..";

    private InputArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Dentro de input.api solamente pueden existir las responsabilidades
     * explícitamente aprobadas para la API:
     *
     * - router
     * - handler
     * - dto
     * - mapper
     * - filter
     * - error
     *
     * Se evita que aparezcan paquetes arbitrarios como:
     *
     * input.api.service
     * input.api.repository
     * input.api.usecase
     * input.api.persistence
     */
    @ArchTest
    static final ArchRule API_MUST_ONLY_CONTAIN_ALLOWED_AREAS =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .resideInAnyPackage(
                            INPUT_API_ROUTER,
                            INPUT_API_HANDLER,
                            INPUT_API_DTO,
                            INPUT_API_MAPPER,
                            INPUT_API_FILTER,
                            INPUT_API_ERROR
                    )
                    .because(
                            "input.api solamente debe contener responsabilidades "
                                    + "propias del adapter HTTP"
                    );

    /**
     * El adapter API solamente puede entrar al negocio mediante port.in.
     *
     * No puede conocer:
     *
     * - port.out
     * - implementaciones concretas de use cases
     * - adapters de salida
     * - configuration/composition root
     */
    @ArchTest
    static final ArchRule API_MUST_NOT_DEPEND_ON_OUTPUT_SIDE =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            PORT_OUT,
                            USE_CASE,
                            OUTPUT,
                            CONFIG
                    )
                    .because(
                            "la API debe acceder al negocio exclusivamente "
                                    + "mediante port.in"
                    );

    /**
     * Ninguna clase de la API puede acceder directamente
     * a tecnologías de persistencia.
     *
     * Esto también incluye tecnologías reactivas como R2DBC.
     *
     * Que R2DBC sea no bloqueante no significa que deba utilizarse
     * directamente desde un handler.
     */
    @ArchTest
    static final ArchRule API_MUST_NOT_DEPEND_ON_PERSISTENCE_APIS =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            /*
                             * Spring Data.
                             */
                            "org.springframework.data..",

                            /*
                             * JDBC / JPA.
                             */
                            "org.springframework.jdbc..",
                            "org.springframework.orm.jpa..",
                            "jakarta.persistence..",
                            "javax.persistence..",
                            "java.sql..",
                            "javax.sql..",
                            "org.hibernate..",

                            /*
                             * R2DBC.
                             */
                            "io.r2dbc..",

                            /*
                             * Redis.
                             */
                            "io.lettuce..",
                            "redis.clients..",

                            /*
                             * Mongo.
                             */
                            "com.mongodb..",

                            /*
                             * Drivers concretos.
                             */
                            "org.postgresql.."
                    )
                    .because(
                            "persistencia y cache son responsabilidades "
                                    + "de adapters de salida"
                    );

    /**
     * El proyecto utiliza exclusivamente WebFlux funcional.
     *
     * Se prohíbe completamente:
     *
     * - @RestController
     * - @RequestMapping
     * - @GetMapping
     * - @PostMapping
     * - @PutMapping
     * - @PatchMapping
     * - @DeleteMapping
     * - @RequestBody
     * - @PathVariable
     * - @RequestParam
     * - @ControllerAdvice
     * - @RestControllerAdvice
     * - @ExceptionHandler
     *
     * La entrada HTTP debe implementarse mediante:
     *
     * RouterFunction -> Handler
     */
    @ArchTest
    static final ArchRule ANNOTATION_BASED_WEB_MODEL_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(SPRING_WEB_ANNOTATIONS)
                    .because(
                            "la API utiliza exclusivamente Spring WebFlux.fn"
                    );

    /**
     * @Controller pertenece a org.springframework.stereotype
     * y no a org.springframework.web.bind.annotation.
     *
     * Por eso necesita una prohibición adicional.
     */
    @ArchTest
    static final ArchRule CONTROLLER_STEREOTYPE_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .beAnnotatedWith(Controller.class)
                    .because(
                            "la API debe implementarse exclusivamente "
                                    + "mediante RouterFunction y Handler"
                    );

    /**
     * El módulo input-api no debe registrar componentes mediante
     * component scanning.
     *
     * El módulo app/config actúa como composition root y es quien
     * construye:
     *
     * - Router
     * - Handler
     * - Mapper
     * - Filters
     * - Error handlers
     */
    @ArchTest
    static final ArchRule API_MUST_NOT_USE_COMPONENT_SCANNING =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .beAnnotatedWith(Component.class)
                    .because(
                            "los componentes del adapter API deben ser "
                                    + "instanciados explícitamente desde app/config"
                    );

    /**
     * input.api tampoco debe contener clases @Configuration.
     *
     * Incorrecto:
     *
     * input.api.router.AuthRouterConfiguration
     *
     * Correcto:
     *
     * config.InputApiConfiguration
     */
    @ArchTest
    static final ArchRule API_MUST_NOT_DECLARE_SPRING_CONFIGURATION =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .beAnnotatedWith(Configuration.class)
                    .because(
                            "toda configuración Spring pertenece al "
                                    + "composition root del módulo app"
                    );

    /**
     * Prohíbe completamente:
     *
     * - Spring MVC.
     * - Servlet.
     * - WebMvc.fn.
     * - DispatcherServlet.
     * - HttpServletRequest.
     * - HttpServletResponse.
     *
     * Únicamente se permite WebFlux.
     */
    @ArchTest
    static final ArchRule SERVLET_AND_SPRING_MVC_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web.servlet..",
                            "org.springframework.boot.web.servlet..",
                            "org.springframework.boot.autoconfigure.web.servlet..",
                            "jakarta.servlet..",
                            "javax.servlet.."
                    )
                    .because(
                            "la API utiliza exclusivamente Spring WebFlux "
                                    + "funcional y no la pila Servlet"
                    );

    /*
     * ================================================================
     * ROUTER
     * ================================================================
     */

    /**
     * Toda clase dentro de api.router debe terminar en Router.
     *
     * Ejemplos:
     *
     * AuthRouter
     * UserRouter
     * SessionRouter
     */
    @ArchTest
    static final ArchRule ROUTER_CLASSES_MUST_HAVE_ROUTER_SUFFIX =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .haveSimpleNameEndingWith("Router")
                    .because(
                            "las clases encargadas del routing HTTP "
                                    + "deben identificarse mediante el sufijo Router"
                    )
                    .allowEmptyShould(true);

    /**
     * Los routers deben ser final.
     *
     * No existe razón arquitectónica para extender un Router.
     */
    @ArchTest
    static final ArchRule ROUTER_CLASSES_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "los routers deben ser componentes cerrados "
                                    + "sin jerarquías de herencia"
                    )
                    .allowEmptyShould(true);

    /**
     * Todo Router debe utilizar la API funcional SERVER de WebFlux.
     *
     * Se espera utilizar tipos como:
     *
     * - RouterFunction
     * - RouterFunctions
     * - RequestPredicate
     * - RequestPredicates
     * - ServerResponse
     */
    @ArchTest
    static final ArchRule ROUTERS_MUST_USE_WEBFLUX_FUNCTIONAL_API =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(WEBFLUX_FUNCTIONAL_SERVER)
                    .because(
                            "los routers deben implementarse mediante "
                                    + "Spring WebFlux.fn"
                    )
                    .allowEmptyShould(true);

    /**
     * Un Router debe delegar la ejecución HTTP a un Handler.
     *
     * Su responsabilidad es únicamente:
     *
     * - Método HTTP.
     * - URI.
     * - Accept.
     * - Content-Type.
     * - Predicados.
     * - Filtros de routing.
     * - Selección del Handler.
     */
    @ArchTest
    static final ArchRule ROUTERS_MUST_DELEGATE_TO_HANDLERS =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .because(
                            "un Router debe enrutar solicitudes "
                                    + "y delegarlas a un Handler"
                    )
                    .allowEmptyShould(true);

    /**
     * El Router NO puede invocar port.in.
     *
     * Incorrecto:
     *
     * Router
     *   -> LoginUseCase
     *
     * Correcto:
     *
     * Router
     *   -> LoginHandler
     *       -> LoginUseCase
     */
    @ArchTest
    static final ArchRule ROUTERS_MUST_NOT_INVOKE_INPUT_PORTS_DIRECTLY =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(PORT_IN)
                    .because(
                            "el Router debe delegar en un Handler "
                                    + "y nunca ejecutar un caso de uso"
                    );

    /**
     * El Router tampoco debería conocer DTOs.
     *
     * Interpretar request/response pertenece al Handler.
     */
    @ArchTest
    static final ArchRule ROUTERS_MUST_NOT_DEPEND_ON_DTOS =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INPUT_API_DTO)
                    .because(
                            "el Router solamente debe definir routing "
                                    + "y no procesar datos HTTP"
                    );

    /**
     * El Router tampoco debe ejecutar mapping.
     */
    @ArchTest
    static final ArchRule ROUTERS_MUST_NOT_DEPEND_ON_MAPPERS =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INPUT_API_MAPPER)
                    .because(
                            "el mapping HTTP debe ejecutarse desde el Handler"
                    );

    /*
     * ================================================================
     * HANDLER
     * ================================================================
     */

    /**
     * Toda clase ubicada dentro de handler debe terminar
     * obligatoriamente en Handler.
     */
    @ArchTest
    static final ArchRule HANDLER_CLASSES_MUST_HAVE_HANDLER_SUFFIX =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .haveSimpleNameEndingWith("Handler")
                    .because(
                            "las clases encargadas de procesar solicitudes "
                                    + "HTTP deben identificarse como Handler"
                    )
                    .allowEmptyShould(true);

    /**
     * Los handlers deben ser final.
     */
    @ArchTest
    static final ArchRule HANDLER_CLASSES_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .because(
                            "los handlers deben ser componentes cerrados "
                                    + "sin jerarquías de herencia"
                    )
                    .allowEmptyShould(true);

    /**
     * Todo Handler debe utilizar la API funcional de WebFlux.
     *
     * Normalmente:
     *
     * ServerRequest
     *       ->
     * Mono<ServerResponse>
     */
    @ArchTest
    static final ArchRule HANDLERS_MUST_USE_WEBFLUX_FUNCTIONAL_API =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(WEBFLUX_FUNCTIONAL_SERVER)
                    .because(
                            "los handlers deben procesar solicitudes mediante "
                                    + "ServerRequest y ServerResponse"
                    )
                    .allowEmptyShould(true);

    /**
     * Los handlers son el único punto del adapter API
     * autorizado para invocar port.in.
     *
     * Flujo:
     *
     * Router
     *   -> Handler
     *       -> port.in
     *           -> usecase
     */
    @ArchTest
    static final ArchRule HANDLERS_MUST_INVOKE_INPUT_PORTS =
            classes()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(PORT_IN)
                    .because(
                            "el Handler debe ejecutar las intenciones "
                                    + "del negocio mediante port.in"
                    )
                    .allowEmptyShould(true);

    /**
     * Prohíbe la dependencia inversa:
     *
     * Handler -> Router
     *
     * La única dirección permitida es:
     *
     * Router -> Handler
     */
    @ArchTest
    static final ArchRule HANDLERS_MUST_NOT_DEPEND_ON_ROUTERS =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INPUT_API_ROUTER)
                    .because(
                            "los handlers no deben conocer cómo "
                                    + "se construyeron las rutas HTTP"
                    );

    /**
     * Un Handler tampoco puede saltarse port.in
     * accediendo directamente al caso de uso concreto.
     *
     * Esta regla es parcialmente redundante con
     * API_MUST_NOT_DEPEND_ON_OUTPUT_SIDE, pero se mantiene
     * como defensa explícita de la relación más importante.
     */
    @ArchTest
    static final ArchRule HANDLERS_MUST_NOT_DEPEND_ON_USE_CASE_IMPLEMENTATIONS =
            noClasses()
                    .that()
                    .resideInAPackage(INPUT_API_HANDLER)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(USE_CASE)
                    .because(
                            "los handlers deben depender de interfaces port.in "
                                    + "y nunca de implementaciones concretas"
                    );
}
