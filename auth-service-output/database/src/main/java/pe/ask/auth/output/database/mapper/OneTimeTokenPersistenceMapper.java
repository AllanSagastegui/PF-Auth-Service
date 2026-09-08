package pe.ask.auth.output.database.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.output.database.entity.OneTimeTokenEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class OneTimeTokenPersistenceMapper {

    public static final OneTimeTokenPersistenceMapper INSTANCE = Mappers.getMapper(OneTimeTokenPersistenceMapper.class);

    @Mapping(target = "type", source = "type", qualifiedByName = "stringToTokenType")
    @Mapping(target = "used", source = "used", defaultValue = "false")
    public abstract OneTimeToken mapToDomain(OneTimeTokenEntity entity);

    @Mapping(target = "type", source = "domain.type", qualifiedByName = "tokenTypeToString")
    @Mapping(target = "newRecord", source = "isNew")
    public abstract OneTimeTokenEntity mapToEntity(OneTimeToken domain, boolean isNew);

    @Named("stringToTokenType")
    protected OneTimeTokenType stringToTokenType(String type) {
        return type != null ? OneTimeTokenType.valueOf(type) : null;
    }

    @Named("tokenTypeToString")
    protected String tokenTypeToString(OneTimeTokenType type) {
        return type != null ? type.name() : null;
    }

    public static OneTimeToken toDomain(OneTimeTokenEntity entity) {
        return INSTANCE.mapToDomain(entity);
    }

    public static OneTimeTokenEntity toEntity(OneTimeToken domain, boolean isNew) {
        return INSTANCE.mapToEntity(domain, isNew);
    }
}
