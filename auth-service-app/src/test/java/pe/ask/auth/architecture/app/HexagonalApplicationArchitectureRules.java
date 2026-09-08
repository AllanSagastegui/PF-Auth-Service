package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static pe.ask.auth.architecture.ArchitecturePackages.CONFIG;
import static pe.ask.auth.architecture.ArchitecturePackages.INPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.MODEL;
import static pe.ask.auth.architecture.ArchitecturePackages.OUTPUT;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN;
import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT;
import static pe.ask.auth.architecture.ArchitecturePackages.USE_CASE;

public final class HexagonalApplicationArchitectureRules {

    private HexagonalApplicationArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Dirección permitida:
     * input -> port.in
     * usecase -> port.in
     * usecase -> port.out
     * usecase -> model
     * output -> port.out
     * output -> model
     * config -> todas las partes
     * Dirección prohibida:
     * core -> input/output/config
     * input -> output/usecase/port.out
     * output -> input/usecase/port.in
     * cualquier módulo -> config
     */
    @ArchTest
    static final ArchRule HEXAGONAL_DEPENDENCIES_MUST_POINT_INWARD =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true)
                    .layer("Model")
                    .definedBy(MODEL)
                    .layer("PortIn")
                    .definedBy(PORT_IN)
                    .layer("PortOut")
                    .definedBy(PORT_OUT)
                    .layer("UseCase")
                    .definedBy(USE_CASE)
                    .layer("Input")
                    .definedBy(INPUT)
                    .layer("Output")
                    .definedBy(OUTPUT)
                    .layer("Config")
                    .definedBy(CONFIG)

                    /*
                     * El modelo puede ser utilizado por el core,
                     * outputs y el composition root.
                     * Input debe trabajar con commands/results,
                     * no directamente con entidades del dominio.
                     */
                    .whereLayer("Model")
                    .mayOnlyBeAccessedByLayers(
                            "PortIn",
                            "PortOut",
                            "UseCase",
                            "Output",
                            "Config"
                    )

                    /*
                     * Los puertos de entrada pueden ser conocidos por:
                     * - input: para invocar el caso de uso;
                     * - usecase: porque implementa el contrato;
                     * - config: para registrar el bean.
                     */
                    .whereLayer("PortIn")
                    .mayOnlyBeAccessedByLayers(
                            "Input",
                            "UseCase",
                            "Config"
                    )

                    /*
                     * Los puertos de salida pueden ser conocidos por:
                     * - usecase: para solicitar capacidades;
                     * - output: porque implementa el contrato;
                     * - config: para componer las implementaciones.
                     */
                    .whereLayer("PortOut")
                    .mayOnlyBeAccessedByLayers(
                            "UseCase",
                            "Output",
                            "Config"
                    )
                    /*
                     * La implementación concreta del caso de uso
                     * solamente puede ser conocida por config.
                     */
                    .whereLayer("UseCase")
                    .mayOnlyBeAccessedByLayers("Config")
                    /*
                     * Los adapters concretos solamente pueden ser
                     * conocidos por el composition root.
                     */
                    .whereLayer("Input")
                    .mayOnlyBeAccessedByLayers("Config")

                    .whereLayer("Output")
                    .mayOnlyBeAccessedByLayers("Config")
                    /*
                     * Config es la capa más externa.
                     * Nadie debe depender de ella.
                     */
                    .whereLayer("Config")
                    .mayNotBeAccessedByAnyLayer()

                    .because(
                            "la arquitectura hexagonal exige que las dependencias "
                                    + "apunten hacia el núcleo y que config actúe "
                                    + "exclusivamente como composition root"
                    );
}
