package pe.ask.auth.output.kafka.adapter;

import org.apache.kafka.clients.producer.ProducerRecord;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.output.kafka.mapper.NotificationMapper;
import pe.ask.auth.output.kafka.message.KafkaProducerEnumMessage;
import pe.ask.auth.output.kafka.message.NotificationMessage;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class NotificationKafkaAdapter implements NotificationOutputPort {

    private final KafkaSender<String, String> kafkaSender;
    private final String topic;

    public NotificationKafkaAdapter(KafkaSender<String, String> kafkaSender, String topic) {
        this.kafkaSender = kafkaSender;
        this.topic = (topic != null && !topic.isBlank()) ? topic : KafkaProducerEnumMessage.TOPIC_NOTIFICATIONS.value();
    }

    public NotificationKafkaAdapter(KafkaSender<String, String> kafkaSender) {
        this(kafkaSender, KafkaProducerEnumMessage.TOPIC_NOTIFICATIONS.value());
    }

    public NotificationKafkaAdapter() {
        this(null, KafkaProducerEnumMessage.TOPIC_NOTIFICATIONS.value());
    }

    @Override
    public Mono<Void> sendVerificationEmail(String email, String token) {
        DomainValidation.requireNonNull(email, KafkaProducerEnumMessage.PARAM_EMAIL.value());
        DomainValidation.requireNonNull(token, KafkaProducerEnumMessage.PARAM_TOKEN.value());

        NotificationMessage message = NotificationMapper.toVerificationMessage(email, token);
        return sendMessage(message.recipient(), message.toString(), KafkaProducerEnumMessage.EVENT_VERIFICATION.value());
    }

    @Override
    public Mono<Void> sendPasswordResetEmail(String email, String token) {
        DomainValidation.requireNonNull(email, KafkaProducerEnumMessage.PARAM_EMAIL.value());
        DomainValidation.requireNonNull(token, KafkaProducerEnumMessage.PARAM_TOKEN.value());

        NotificationMessage message = NotificationMapper.toPasswordResetMessage(email, token);
        return sendMessage(message.recipient(), message.toString(), KafkaProducerEnumMessage.EVENT_PASSWORD_RESET.value());
    }

    private Mono<Void> sendMessage(String key, String payload, String eventType) {
        if (kafkaSender == null) {
            return Mono.empty();
        }
        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.hasKey(KafkaProducerEnumMessage.CONTEXT_CORRELATION_ID.value())
                    ? ctx.get(KafkaProducerEnumMessage.CONTEXT_CORRELATION_ID.value())
                    : generateSafeUuid();
            String requestId = ctx.hasKey(KafkaProducerEnumMessage.CONTEXT_REQUEST_ID.value())
                    ? ctx.get(KafkaProducerEnumMessage.CONTEXT_REQUEST_ID.value())
                    : generateSafeUuid();

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);
            record.headers().add(KafkaProducerEnumMessage.HEADER_CORRELATION_ID.value(), correlationId.getBytes(StandardCharsets.UTF_8));
            record.headers().add(KafkaProducerEnumMessage.HEADER_REQUEST_ID.value(), requestId.getBytes(StandardCharsets.UTF_8));
            record.headers().add(KafkaProducerEnumMessage.HEADER_TRACE_ID.value(), correlationId.getBytes(StandardCharsets.UTF_8));
            record.headers().add(KafkaProducerEnumMessage.HEADER_EVENT_TYPE.value(), eventType.getBytes(StandardCharsets.UTF_8));
            record.headers().add(KafkaProducerEnumMessage.HEADER_TIMESTAMP.value(), String.valueOf(Instant.now().toEpochMilli()).getBytes(StandardCharsets.UTF_8));

            SenderRecord<String, String, Void> senderRecord = SenderRecord.create(record, null);
            return kafkaSender.send(Mono.just(senderRecord)).then();
        });
    }

    private static String generateSafeUuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }
}
