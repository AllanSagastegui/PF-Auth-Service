package pe.ask.auth.output.database.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.SessionStatus;
import pe.ask.auth.output.database.entity.SessionEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class SessionPersistenceMapper {

    public static final SessionPersistenceMapper INSTANCE = Mappers.getMapper(SessionPersistenceMapper.class);

    @Mapping(target = "status", source = "status", qualifiedByName = "stringToSessionStatus")
    public abstract Session mapToDomain(SessionEntity entity);

    @Mapping(target = "status", source = "domain.status", qualifiedByName = "sessionStatusToString")
    @Mapping(target = "newRecord", source = "isNew")
    public abstract SessionEntity mapToEntity(Session domain, boolean isNew);

    @Named("stringToSessionStatus")
    protected SessionStatus stringToSessionStatus(String status) {
        return status != null ? SessionStatus.valueOf(status) : null;
    }

    @Named("sessionStatusToString")
    protected String sessionStatusToString(SessionStatus status) {
        return status != null ? status.name() : null;
    }

    public static Session toDomain(SessionEntity entity) {
        return INSTANCE.mapToDomain(entity);
    }

    public static SessionEntity toEntity(Session domain, boolean isNew) {
        return INSTANCE.mapToEntity(domain, isNew);
    }
}
