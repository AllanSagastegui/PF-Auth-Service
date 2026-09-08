package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;

public final class ReactiveTechnologyArchitectureRules {

    private ReactiveTechnologyArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Prohíbe:
     * - Servlet.
     * - Spring MVC.
     * - WebMvc.fn.
     */
    @ArchTest
    static final ArchRule SERVLET_AND_SPRING_MVC_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.servlet..",
                            "javax.servlet..",
                            "org.springframework.web.servlet..",
                            "org.springframework.boot.web.servlet..",
                            "org.springframework.boot.autoconfigure.web.servlet.."
                    )
                    .because(
                            "el microservicio utiliza exclusivamente "
                                    + "Spring WebFlux funcional"
                    );

    /**
     * Prohíbe el modelo de controllers anotados.
     */
    @ArchTest
    static final ArchRule ANNOTATION_BASED_WEB_MODEL_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(
                            "org.springframework.web.bind.annotation.."
                    )
                    .because(
                            "la entrada HTTP debe implementarse únicamente "
                                    + "con RouterFunction y handlers funcionales"
                    );

    /**
     * Persistencia imperativa prohibida.
     * Permitidos:
     * - R2DBC.
     * - ReactiveRedisTemplate.
     * - ReactiveMongoTemplate.
     * - Repositorios ReactiveCrudRepository.
     */
    @ArchTest
    static final ArchRule BLOCKING_PERSISTENCE_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "java.sql..",
                            "javax.sql..",
                            "jakarta.persistence..",
                            "javax.persistence..",
                            "org.springframework.jdbc..",
                            "org.springframework.orm.jpa..",
                            "org.springframework.data.jdbc..",
                            "org.springframework.data.jpa..",
                            "org.hibernate..",
                            "org.mybatis..",
                            "com.zaxxer.hikari..",
                            "com.mongodb.client..",
                            "redis.clients.jedis..",
                            "io.lettuce.core.api.sync.."
                    )
                    .because(
                            "toda persistencia debe utilizar drivers "
                                    + "y APIs realmente reactivas"
                    );

    /**
     * Clientes HTTP imperativos prohibidos.
     * El estándar será WebClient/Reactor Netty.
     */
    @ArchTest
    static final ArchRule BLOCKING_HTTP_CLIENTS_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web.client..",
                            "org.springframework.cloud.openfeign..",
                            "feign..",
                            "org.apache.http..",
                            "org.apache.hc.client5.http.classic..",
                            "okhttp3..",
                            "java.net.http..",
                            "software.amazon.awssdk.http.urlconnection..",
                            "software.amazon.awssdk.http.apache.."
                    )
                    .because(
                            "las llamadas HTTP deben realizarse mediante "
                                    + "WebClient o Reactor Netty"
                    );

    /**
     * Migraciones dentro del proceso de la aplicación bloquean
     * durante el arranque.
     * En este estándar estricto deben ejecutarse en pipeline,
     * job o init container separado.
     */
    @ArchTest
    static final ArchRule BLOCKING_DATABASE_MIGRATIONS_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.flywaydb..",
                            "liquibase.."
                    )
                    .because(
                            "las migraciones bloqueantes deben ejecutarse "
                                    + "fuera del proceso WebFlux"
                    );

    /**
     * Prohíbe acceso directo a filesystem.
     * Si el servicio necesita almacenar archivos, debe utilizar
     * un cliente verdaderamente reactivo y trabajar con DataBuffer
     * o Publisher de buffers.
     */
    @ArchTest
    static final ArchRule BLOCKING_FILE_SYSTEM_APIS_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("java.nio.file..")
                    .because(
                            "java.nio.file realiza operaciones bloqueantes "
                                    + "sobre el filesystem"
                    );

    /**
     * JMS está basado en una API imperativa.
     */
    @ArchTest
    static final ArchRule JMS_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.jms..",
                            "javax.jms..",
                            "org.springframework.jms.."
                    )
                    .because(
                            "la mensajería debe utilizar clientes "
                                    + "asíncronos o reactivos"
                    );

    /**
     * Tipos imperativos específicos que comparten paquete
     * con alternativas reactivas.
     */
    @ArchTest
    static final ArchRule IMPERATIVE_TYPES_MUST_NOT_BE_USED =
            classes()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should(
                            NoDependenciesOnTypesCondition.notDependOn(
                                    /*
                                     * Spring Data.
                                     */
                                    "org.springframework.data.repository.CrudRepository",
                                    "org.springframework.data.repository.PagingAndSortingRepository",
                                    "org.springframework.data.redis.core.RedisTemplate",
                                    "org.springframework.data.redis.core.StringRedisTemplate",
                                    "org.springframework.data.mongodb.core.MongoTemplate",

                                    /*
                                     * Transacciones imperativas.
                                     */
                                    "org.springframework.transaction.PlatformTransactionManager",
                                    "org.springframework.transaction.support.TransactionTemplate",

                                    /*
                                     * Filesystem.
                                     */
                                    "java.io.File",
                                    "java.io.FileInputStream",
                                    "java.io.FileOutputStream",
                                    "java.io.RandomAccessFile",

                                    /*
                                     * Networking imperativo.
                                     */
                                    "java.net.Socket",
                                    "java.net.ServerSocket",
                                    "java.net.URLConnection",
                                    "java.net.HttpURLConnection"
                            )
                    )
                    .because(
                            "deben utilizarse las equivalencias reactivas "
                                    + "de persistencia, transacciones y networking"
                    );
}
