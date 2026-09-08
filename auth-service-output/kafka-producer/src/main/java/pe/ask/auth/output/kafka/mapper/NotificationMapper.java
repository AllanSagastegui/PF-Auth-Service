package pe.ask.auth.output.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.output.kafka.message.KafkaProducerEnumMessage;
import pe.ask.auth.output.kafka.message.NotificationMessage;

import java.time.Instant;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class NotificationMapper {

    public static final NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    public abstract NotificationMessage mapToMessage(String eventType, String recipient, String token, long timestamp);

    public static NotificationMessage toVerificationMessage(String email, String token) {
        return INSTANCE.mapToMessage(
                KafkaProducerEnumMessage.EVENT_VERIFICATION.value(),
                email,
                token,
                Instant.now().toEpochMilli()
        );
    }

    public static NotificationMessage toPasswordResetMessage(String email, String token) {
        return INSTANCE.mapToMessage(
                KafkaProducerEnumMessage.EVENT_PASSWORD_RESET.value(),
                email,
                token,
                Instant.now().toEpochMilli()
        );
    }
}
