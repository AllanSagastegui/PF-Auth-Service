package pe.ask.auth.output.kafka.message;

public enum KafkaProducerEnumMessage {
    TOPIC_NOTIFICATIONS("auth.notifications"),
    TOPIC_AUDIT("auth.audit"),
    DEFAULT_BOOTSTRAP_SERVERS("localhost:9092"),
    EVENT_VERIFICATION("VERIFICATION_EMAIL"),
    EVENT_PASSWORD_RESET("PASSWORD_RESET_EMAIL"),
    HEADER_CORRELATION_ID("X-Correlation-Id"),
    HEADER_REQUEST_ID("X-Request-Id"),
    HEADER_TRACE_ID("traceId"),
    HEADER_TIMESTAMP("timestamp"),
    HEADER_EVENT_TYPE("eventType"),
    CONTEXT_CORRELATION_ID("correlationId"),
    CONTEXT_REQUEST_ID("requestId"),
    DEFAULT_CORRELATION("anonymous"),
    PARAM_EMAIL("email"),
    PARAM_TOKEN("token"),
    PARAM_EVENT("event");

    private final String value;

    KafkaProducerEnumMessage(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
