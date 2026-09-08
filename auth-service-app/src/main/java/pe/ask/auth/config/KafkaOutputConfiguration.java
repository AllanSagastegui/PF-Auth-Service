package pe.ask.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.ask.auth.config.properties.AuthKafkaProperties;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.SecurityAuditOutputPort;
import pe.ask.auth.output.kafka.adapter.NotificationKafkaAdapter;
import pe.ask.auth.output.kafka.adapter.SecurityAuditKafkaAdapter;
import pe.ask.auth.output.kafka.message.KafkaProducerEnumMessage;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public final class KafkaOutputConfiguration {

    @Bean
    public KafkaSender<String, String> kafkaSender(AuthKafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        String servers = kafkaProperties != null ? kafkaProperties.bootstrapServers() : KafkaProducerEnumMessage.DEFAULT_BOOTSTRAP_SERVERS.value();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public NotificationOutputPort notificationOutputPort(KafkaSender<String, String> kafkaSender, AuthKafkaProperties kafkaProperties) {
        String topic = kafkaProperties != null ? kafkaProperties.notificationsTopic() : null;
        return new NotificationKafkaAdapter(kafkaSender, topic);
    }

    @Bean
    public SecurityAuditOutputPort securityAuditOutputPort(KafkaSender<String, String> kafkaSender, AuthKafkaProperties kafkaProperties) {
        String topic = kafkaProperties != null ? kafkaProperties.auditTopic() : null;
        return new SecurityAuditKafkaAdapter(kafkaSender, topic);
    }
}
