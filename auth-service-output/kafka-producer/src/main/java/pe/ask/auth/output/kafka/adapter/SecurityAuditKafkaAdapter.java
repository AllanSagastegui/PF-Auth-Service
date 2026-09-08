package pe.ask.auth.output.kafka.adapter;

import org.apache.kafka.clients.producer.ProducerRecord;
import pe.ask.auth.core.port.out.SecurityAuditOutputPort;
import pe.ask.auth.output.kafka.mapper.AuditMapper;
import pe.ask.auth.output.kafka.message.AuditMessage;
import pe.ask.auth.output.kafka.message.KafkaProducerEnumMessage;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SecurityAuditKafkaAdapter implements SecurityAuditOutputPort {

    private final KafkaSender<String, String> kafkaSender;
    private final String topic;

    public SecurityAuditKafkaAdapter(KafkaSender<String, String> kafkaSender, String topic) {
        this.kafkaSender = kafkaSender;
        this.topic = (topic != null && !topic.isBlank()) ? topic : KafkaProducerEnumMessage.TOPIC_AUDIT.value();
    }

    public SecurityAuditKafkaAdapter(KafkaSender<String, String> kafkaSender) {
        this(kafkaSender, KafkaProducerEnumMessage.TOPIC_AUDIT.value());
    }

    public SecurityAuditKafkaAdapter() {
        this(null, KafkaProducerEnumMessage.TOPIC_AUDIT.value());
    }

    @Override
    public Mono<Void> recordSecurityEvent(String eventType, UUID userId, String detail, Instant timestamp) {
        if (kafkaSender == null) {
            return Mono.empty();
        }
        AuditMessage message = AuditMapper.toMessage(eventType, userId, detail, timestamp);
        String key = userId != null ? userId.toString() : KafkaProducerEnumMessage.DEFAULT_CORRELATION.value();

        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.hasKey(KafkaProducerEnumMessage.CONTEXT_CORRELATION_ID.value())
                    ? ctx.get(KafkaProducerEnumMessage.CONTEXT_CORRELATION_ID.value())
                    : generateSafeUuid();
            String requestId = ctx.hasKey(KafkaProducerEnumMessage.CONTEXT_REQUEST_ID.value())
                    ? ctx.get(KafkaProducerEnumMessage.CONTEXT_REQUEST_ID.value())
                    : generateSafeUuid();

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, message.toString());
            producerRecord.headers().add(KafkaProducerEnumMessage.HEADER_CORRELATION_ID.value(), correlationId.getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add(KafkaProducerEnumMessage.HEADER_REQUEST_ID.value(), requestId.getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add(KafkaProducerEnumMessage.HEADER_TRACE_ID.value(), correlationId.getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add(KafkaProducerEnumMessage.HEADER_EVENT_TYPE.value(), (eventType != null ? eventType : "").getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add(KafkaProducerEnumMessage.HEADER_TIMESTAMP.value(), String.valueOf(timestamp != null ? timestamp.toEpochMilli() : Instant.now().toEpochMilli()).getBytes(StandardCharsets.UTF_8));

            SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
            return kafkaSender.send(Mono.just(senderRecord)).then();
        });
    }

    private static String generateSafeUuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }
}
