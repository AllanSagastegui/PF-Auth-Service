package pe.ask.auth.architecture.output.database;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.*;

public final class DatabaseArchitectureRules {

    private DatabaseArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Database / Persistence solamente puede contener:
     * - adapter
     * - repository
     * - entity
     * - mapper
     */
    @ArchTest
    static final ArchRule DATABASE_MUST_ONLY_CONTAIN_ALLOWED_AREAS =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE
                    )
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER,
                            OUTPUT_PERSISTENCE_REPOSITORY,
                            OUTPUT_DATABASE_REPOSITORY,
                            OUTPUT_PERSISTENCE_ENTITY,
                            OUTPUT_DATABASE_ENTITY,
                            OUTPUT_PERSISTENCE_MAPPER,
                            OUTPUT_DATABASE_MAPPER
                    )
                    .because(
                            "database/persistence debe separar adapter, repository, "
                                    + "entity y mapper"
                    );

    /**
     * JDBC/JPA imperativos están absolutamente prohibidos.
     */
    @ArchTest
    static final ArchRule BLOCKING_DATABASE_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE
                    )
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
                            "com.zaxxer.hikari.."
                    )
                    .because(
                            "la persistencia debe ser end-to-end reactiva (e.g. R2DBC)"
                    );

    /**
     * Repositories solamente pueden existir en el paquete repository.
     */
    @ArchTest
    static final ArchRule REPOSITORIES_MUST_RESIDE_IN_REPOSITORY_PACKAGE =
            classes()
                    .that()
                    .areAnnotatedWith(Repository.class)
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_REPOSITORY,
                            OUTPUT_DATABASE_REPOSITORY
                    )
                    .because(
                            "@Repository pertenece a la infraestructura de base de datos"
                    )
                    .allowEmptyShould(true);

    /**
     * Convención para repositories.
     */
    @ArchTest
    static final ArchRule REPOSITORIES_MUST_END_WITH_REPOSITORY =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_REPOSITORY,
                            OUTPUT_DATABASE_REPOSITORY
                    )
                    .should()
                    .haveSimpleNameEndingWith("Repository")
                    .because(
                            "los repositories deben ser explícitamente identificables"
                    )
                    .allowEmptyShould(true);

    /**
     * Entidades exclusivamente dentro del paquete entity.
     */
    @ArchTest
    static final ArchRule ENTITIES_MUST_RESIDE_IN_ENTITY_PACKAGE =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Entity")
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ENTITY,
                            OUTPUT_DATABASE_ENTITY
                    )
                    .because(
                            "Entity representa un modelo de persistencia/tabla"
                    )
                    .allowEmptyShould(true);

    /**
     * Y todo lo ubicado en entity debe llamarse Entity.
     */
    @ArchTest
    static final ArchRule PERSISTENCE_ENTITY_CLASSES_MUST_END_WITH_ENTITY =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ENTITY,
                            OUTPUT_DATABASE_ENTITY
                    )
                    .should()
                    .haveSimpleNameEndingWith("Entity")
                    .because(
                            "las representaciones persistidas deben distinguirse del modelo de dominio"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule MAPPERS_MUST_END_WITH_MAPPER =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_MAPPER,
                            OUTPUT_DATABASE_MAPPER
                    )
                    .should()
                    .haveSimpleNameEndingWith("Mapper")
                    .because(
                            "los mappers convierten explícitamente entre Entity y Domain"
                    )
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule DATABASE_ADAPTERS_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_ADAPTER,
                            OUTPUT_DATABASE_ADAPTER
                    )
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule DATABASE_REPOSITORIES_MUST_NOT_USE_JPA_REPOSITORY =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_REPOSITORY,
                            OUTPUT_DATABASE_REPOSITORY
                    )
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.springframework.data.jpa.repository.JpaRepository"
                    );

    @ArchTest
    static final ArchRule DATABASE_REPOSITORIES_MUST_NOT_USE_IMPERATIVE_REPOSITORIES =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE_REPOSITORY,
                            OUTPUT_DATABASE_REPOSITORY
                    )
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.springframework.data.repository.CrudRepository"
                    )
                    .because(
                            "los repositories deben utilizar únicamente abstracciones reactivas (ReactiveCrudRepository, R2dbcRepository)"
                    );
}
