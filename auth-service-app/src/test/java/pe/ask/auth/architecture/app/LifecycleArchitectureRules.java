package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;

public final class LifecycleArchitectureRules {

    private LifecycleArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    @ArchTest
    static final ArchRule APPLICATION_RUNNER_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAssignableTo(ApplicationRunner.class)
                    .because(
                            "el arranque de la aplicación no debe ejecutar "
                                    + "flujos de negocio"
                    );

    @ArchTest
    static final ArchRule COMMAND_LINE_RUNNER_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAssignableTo(CommandLineRunner.class)
                    .because(
                            "el arranque de la aplicación no debe ejecutar "
                                    + "flujos de negocio"
                    );

    @ArchTest
    static final ArchRule INITIALIZING_BEAN_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAssignableTo(InitializingBean.class)
                    .because(
                            "la inicialización de beans no debe utilizarse "
                                    + "para realizar I/O o ejecutar casos de uso"
                    );

    @ArchTest
    static final ArchRule APPLICATION_LISTENER_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAssignableTo(ApplicationListener.class)
                    .because(
                            "los eventos de arranque no deben utilizarse "
                                    + "para iniciar subscriptions manuales"
                    );

    @ArchTest
    static final ArchRule SMART_LIFECYCLE_MUST_NOT_BE_IMPLEMENTED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAssignableTo(SmartLifecycle.class)
                    .because(
                            "el ciclo de vida reactivo debe ser administrado "
                                    + "por Spring y los drivers correspondientes"
                    );

    @ArchTest
    static final ArchRule EVENT_LISTENER_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(EventListener.class.getName())
                    .because(
                            "los listeners imperativos pueden introducir "
                                    + "subscriptions y operaciones ocultas"
                    );

    @ArchTest
    static final ArchRule POST_CONSTRUCT_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("jakarta.annotation.PostConstruct")
                    .because(
                            "@PostConstruct no debe iniciar operaciones "
                                    + "de negocio o infraestructura"
                    );

    @ArchTest
    static final ArchRule ASYNC_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(Async.class.getName())
                    .because(
                            "la concurrencia debe modelarse con Reactor "
                                    + "y no mediante @Async"
                    );

    @ArchTest
    static final ArchRule SCHEDULED_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(Scheduled.class.getName())
                    .because(
                            "los procesos periódicos deben modelarse "
                                    + "de forma reactiva o ejecutarse fuera "
                                    + "del microservicio"
                    );
}
