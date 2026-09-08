package pe.ask.auth.output.database.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.output.database.entity.UserEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class UserPersistenceMapper {

    private static final String ROLE_DELIMITER = ",";

    public static final UserPersistenceMapper INSTANCE = Mappers.getMapper(UserPersistenceMapper.class);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "stringToRoles")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "mfaEnabled", source = "mfaEnabled", defaultValue = "false")
    @Mapping(target = "authVersion", source = "authVersion", defaultValue = "1L")
    public abstract User mapToDomain(UserEntity entity);

    @Mapping(target = "roles", source = "domain.roles", qualifiedByName = "rolesToString")
    @Mapping(target = "status", source = "domain.status", qualifiedByName = "statusToString")
    @Mapping(target = "createdAt", source = "domain.createdAt")
    @Mapping(target = "updatedAt", source = "domain.updatedAt")
    @Mapping(target = "newRecord", source = "isNew")
    public abstract UserEntity mapToEntity(User domain, boolean isNew);

    protected LocalDateTime map(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    protected Instant map(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC) : null;
    }

    @Named("stringToRoles")
    protected Set<Role> stringToRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roles.split(ROLE_DELIMITER))
                .map(String::strip)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    @Named("rolesToString")
    protected String rolesToString(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(ROLE_DELIMITER));
    }

    @Named("stringToStatus")
    protected UserStatus stringToStatus(String status) {
        return status != null ? UserStatus.valueOf(status) : null;
    }

    @Named("statusToString")
    protected String statusToString(UserStatus status) {
        return status != null ? status.name() : null;
    }

    public static User toDomain(UserEntity entity) {
        return INSTANCE.mapToDomain(entity);
    }

    public static UserEntity toEntity(User domain, boolean isNew) {
        return INSTANCE.mapToEntity(domain, isNew);
    }
}
