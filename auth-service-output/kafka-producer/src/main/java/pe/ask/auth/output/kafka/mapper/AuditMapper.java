package pe.ask.auth.output.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.output.kafka.message.AuditMessage;

import java.time.Instant;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class AuditMapper {

    public static final AuditMapper INSTANCE = Mappers.getMapper(AuditMapper.class);

    public abstract AuditMessage mapToMessage(String eventType, UUID userId, String detail, Instant timestamp);

    public static AuditMessage toMessage(String eventType, UUID userId, String detail, Instant timestamp) {
        return INSTANCE.mapToMessage(eventType, userId, detail, timestamp);
    }
}
