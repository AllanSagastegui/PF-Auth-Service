package pe.ask.auth.output.database.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.model.RefreshTokenStatus;
import pe.ask.auth.output.database.entity.RefreshTokenEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class RefreshTokenPersistenceMapper {

    public static final RefreshTokenPersistenceMapper INSTANCE = Mappers.getMapper(RefreshTokenPersistenceMapper.class);

    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "rotatedAt", source = "revokedAt")
    @Mapping(target = "sequenceNumber", source = "version", defaultValue = "1L")
    public abstract RefreshToken mapToDomain(RefreshTokenEntity entity);

    @Mapping(target = "status", source = "domain.status", qualifiedByName = "statusToString")
    @Mapping(target = "revokedAt", source = "domain.rotatedAt")
    @Mapping(target = "version", source = "domain.sequenceNumber")
    @Mapping(target = "newRecord", source = "isNew")
    public abstract RefreshTokenEntity mapToEntity(RefreshToken domain, boolean isNew);

    @Named("stringToStatus")
    protected RefreshTokenStatus stringToStatus(String status) {
        return status != null ? RefreshTokenStatus.valueOf(status) : null;
    }

    @Named("statusToString")
    protected String statusToString(RefreshTokenStatus status) {
        return status != null ? status.name() : null;
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        return INSTANCE.mapToDomain(entity);
    }

    public static RefreshTokenEntity toEntity(RefreshToken domain, boolean isNew) {
        return INSTANCE.mapToEntity(domain, isNew);
    }
}
