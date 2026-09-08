package pe.ask.auth.architecture.output.security;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.*;

public final class SecurityArchitectureRules {

    private SecurityArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Security solamente puede contener:
     * - adapter
     * - model
     */
    @ArchTest
    static final ArchRule SECURITY_MUST_ONLY_CONTAIN_ALLOWED_AREAS =
            classes()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY)
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_SECURITY_ADAPTER,
                            OUTPUT_SECURITY_MODEL
                    )
                    .because(
                            "output.security solamente debe implementar capacidades técnicas de seguridad"
                    );

    /**
     * Un security adapter no puede conocer HTTP.
     */
    @ArchTest
    static final ArchRule SECURITY_MUST_NOT_DEPEND_ON_HTTP =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http.."
                    )
                    .because(
                            "output.security implementa capacidades independientes del protocolo HTTP"
                    );

    /**
     * Tampoco debe configurar la security filter chain web.
     */
    @ArchTest
    static final ArchRule SECURITY_OUTPUT_MUST_NOT_CONFIGURE_WEB_SECURITY =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.security.web..",
                            "org.springframework.security.config.web.server.."
                    )
                    .because(
                            "la configuración de seguridad HTTP pertenece al módulo app/config"
                    );

    /**
     * Seguridad tampoco puede acceder directamente a base de datos.
     */
    @ArchTest
    static final ArchRule SECURITY_MUST_NOT_DEPEND_ON_PERSISTENCE =
            noClasses()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            OUTPUT_PERSISTENCE,
                            OUTPUT_DATABASE,
                            "io.r2dbc..",
                            "org.springframework.data.."
                    )
                    .because(
                            "security solamente implementa capacidades criptográficas o de tokens"
                    );

    @ArchTest
    static final ArchRule SECURITY_ADAPTERS_MUST_END_WITH_ADAPTER =
            classes()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY_ADAPTER)
                    .should()
                    .haveSimpleNameEndingWith("Adapter")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule SECURITY_ADAPTERS_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAPackage(OUTPUT_SECURITY_ADAPTER)
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .allowEmptyShould(true);
}
