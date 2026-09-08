package pe.ask.auth.output.database.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.output.database.entity.OutboxMessageEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class OutboxMessagePersistenceMapper {

    public static final OutboxMessagePersistenceMapper INSTANCE = Mappers.getMapper(OutboxMessagePersistenceMapper.class);

    @Mapping(target = "processedAt", source = "publishedAt")
    @Mapping(target = "retryCount", source = "attempts", defaultValue = "0")
    public abstract OutboxMessage mapToDomain(OutboxMessageEntity entity);

    @Mapping(target = "publishedAt", source = "domain.processedAt")
    @Mapping(target = "attempts", source = "domain.retryCount")
    @Mapping(target = "newRecord", source = "isNew")
    public abstract OutboxMessageEntity mapToEntity(OutboxMessage domain, boolean isNew);

    public static OutboxMessage toDomain(OutboxMessageEntity entity) {
        return INSTANCE.mapToDomain(entity);
    }

    public static OutboxMessageEntity toEntity(OutboxMessage domain, boolean isNew) {
        return INSTANCE.mapToEntity(domain, isNew);
    }
}
