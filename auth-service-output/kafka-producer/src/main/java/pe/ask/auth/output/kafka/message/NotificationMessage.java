package pe.ask.auth.output.kafka.message;

public record NotificationMessage(
        String eventType,
        String recipient,
        String token,
        long timestamp
) {
}
