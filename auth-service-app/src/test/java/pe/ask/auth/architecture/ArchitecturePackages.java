package pe.ask.auth.architecture;

public final class ArchitecturePackages {

    private ArchitecturePackages() {
        throw new IllegalStateException("Architecture constants class");
    }

    /*
     * Aplicación completa.
     */
    public static final String ROOT =
            "pe.ask.auth";

    public static final String APPLICATION =
            ROOT + "..";

    /*
     * Paquete exacto donde debe ubicarse MainApplication.
     */
    public static final String BOOTSTRAP =
            ROOT;

    /*
     * Composition root / Config.
     */
    public static final String CONFIG =
            ROOT + ".config..";

    public static final String CONFIG_PROPERTIES =
            ROOT + ".config.properties..";

    /*
     * Core.
     */
    public static final String CORE =
            ROOT + ".core..";

    public static final String MODEL =
            ROOT + ".core.model..";

    /*
     * Port In
     */
    public static final String PORT_IN_PACKAGE =
            ROOT + ".core.port.in";

    public static final String PORT_IN =
            PORT_IN_PACKAGE + "..";

    public static final String PORT_IN_COMMAND =
            ROOT + ".core.port.in.command..";

    public static final String PORT_IN_RESULT =
            ROOT + ".core.port.in.result..";

    /*
     * Port Out
     */
    public static final String PORT_OUT_PACKAGE =
            ROOT + ".core.port.out";

    public static final String PORT_OUT =
            PORT_OUT_PACKAGE + "..";

    /*
     * Use cases
     */
    public static final String USE_CASE_PACKAGE =
            ROOT + ".core.usecase";

    public static final String USE_CASE =
            USE_CASE_PACKAGE + "..";

    public static final String USE_CASE_ANNOTATION =
            ROOT + ".core.usecase.annotation..";

    /*
     * INPUT
     */
    public static final String INPUT =
            ROOT + ".input..";

    /*
     * Input - API
     */
    public static final String INPUT_API =
            ROOT + ".input.api..";

    public static final String INPUT_API_ROUTER =
            ROOT + ".input.api.router..";

    public static final String INPUT_API_HANDLER =
            ROOT + ".input.api.handler..";

    public static final String INPUT_API_DTO =
            ROOT + ".input.api.dto..";

    public static final String INPUT_DTO =
            INPUT_API_DTO;

    public static final String INPUT_API_REQUEST =
            ROOT + ".input.api.dto.request..";

    public static final String INPUT_API_RESPONSE =
            ROOT + ".input.api.dto.response..";

    public static final String INPUT_API_MAPPER =
            ROOT + ".input.api.mapper..";

    public static final String INPUT_API_FILTER =
            ROOT + ".input.api.filter..";

    public static final String INPUT_API_ERROR =
            ROOT + ".input.api.error..";

    /*
     * OUTPUT
     */
    public static final String OUTPUT =
            ROOT + ".output..";

    /*
     * Output - Database / Persistence
     */
    public static final String OUTPUT_DATABASE =
            ROOT + ".output.database..";

    public static final String OUTPUT_PERSISTENCE =
            ROOT + ".output.persistence..";

    public static final String OUTPUT_DATABASE_ADAPTER =
            ROOT + ".output.database.adapter..";

    public static final String OUTPUT_PERSISTENCE_ADAPTER =
            ROOT + ".output.persistence.adapter..";

    public static final String OUTPUT_DATABASE_REPOSITORY =
            ROOT + ".output.database.repository..";

    public static final String OUTPUT_PERSISTENCE_REPOSITORY =
            ROOT + ".output.persistence.repository..";

    public static final String OUTPUT_DATABASE_ENTITY =
            ROOT + ".output.database.entity..";

    public static final String OUTPUT_PERSISTENCE_ENTITY =
            ROOT + ".output.persistence.entity..";

    public static final String OUTPUT_ENTITY =
            OUTPUT_PERSISTENCE_ENTITY;

    public static final String OUTPUT_DATABASE_MAPPER =
            ROOT + ".output.database.mapper..";

    public static final String OUTPUT_PERSISTENCE_MAPPER =
            ROOT + ".output.persistence.mapper..";

    /*
     * Output - Producer / Messaging / Kafka
     */
    public static final String OUTPUT_MESSAGING =
            ROOT + ".output.messaging..";

    public static final String OUTPUT_KAFKA =
            ROOT + ".output.messaging.kafka..";

    public static final String OUTPUT_PRODUCER =
            ROOT + ".output.kafka..";

    public static final String OUTPUT_KAFKA_ADAPTER =
            ROOT + ".output.messaging.kafka.adapter..";

    public static final String OUTPUT_PRODUCER_ADAPTER =
            ROOT + ".output.kafka.adapter..";

    public static final String OUTPUT_KAFKA_MESSAGE =
            ROOT + ".output.messaging.kafka.message..";

    public static final String OUTPUT_PRODUCER_MESSAGE =
            ROOT + ".output.kafka.message..";

    public static final String OUTPUT_KAFKA_MAPPER =
            ROOT + ".output.messaging.kafka.mapper..";

    public static final String OUTPUT_PRODUCER_MAPPER =
            ROOT + ".output.kafka.mapper..";

    /*
     * Output - Security
     */
    public static final String OUTPUT_SECURITY =
            ROOT + ".output.security..";

    public static final String OUTPUT_SECURITY_ADAPTER =
            ROOT + ".output.security.adapter..";

    public static final String OUTPUT_SECURITY_MODEL =
            ROOT + ".output.security.model..";
}
