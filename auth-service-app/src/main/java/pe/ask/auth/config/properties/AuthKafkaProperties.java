package pe.ask.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.ask.auth.output.kafka.message.KafkaProducerEnumMessage;

@ConfigurationProperties(prefix = "app.kafka")
public record AuthKafkaProperties(
        String bootstrapServers,
        String notificationsTopic,
        String auditTopic
) {
    public AuthKafkaProperties {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = KafkaProducerEnumMessage.DEFAULT_BOOTSTRAP_SERVERS.value();
        }
        if (notificationsTopic == null || notificationsTopic.isBlank()) {
            notificationsTopic = KafkaProducerEnumMessage.TOPIC_NOTIFICATIONS.value();
        }
        if (auditTopic == null || auditTopic.isBlank()) {
            auditTopic = KafkaProducerEnumMessage.TOPIC_AUDIT.value();
        }
    }
}
