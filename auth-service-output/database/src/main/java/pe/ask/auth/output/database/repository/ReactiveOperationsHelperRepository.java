package pe.ask.auth.output.database.repository;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import pe.ask.auth.core.model.exception.DatabaseOperationException;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.output.database.entity.DatabaseEnumEntity;
import pe.ask.core.mapper.EntityMapper;
import pe.ask.core.mapper.impl.GenericBeanMapper;
import pe.ask.core.model.pagination.PageResponse;
import pe.ask.persistence.core.entity.Entity;
import pe.ask.persistence.core.repository.R2DBCHelperRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.FeatureDescriptor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class ReactiveOperationsHelperRepository<
        D,
        E extends Entity,
        R extends R2DBCHelperRepository<E>
        > {

    protected final R repository;
    protected final String idFieldName = DatabaseEnumEntity.FIELD_ID.value();

    private final EntityMapper<D, E> mapper;

    protected ReactiveOperationsHelperRepository(R repository, EntityMapper<D, E> mapper, Class<D> domainClass, Class<E> entityClass) {
        this.repository = repository;
        if (mapper != null) {
            this.mapper = mapper;
        } else if (domainClass != null && entityClass != null) {
            this.mapper = new GenericBeanMapper<>(domainClass, entityClass);
        } else {
            this.mapper = null;
        }
    }

    protected ReactiveOperationsHelperRepository(R repository) {
        this(repository, null, null, null);
    }

    protected R getRepository() {
        return repository;
    }

    protected E toEntity(D domain) {
        if (mapper == null) {
            throw new DatabaseOperationException(DatabaseEnumEntity.MSG_MAPPER_NOT_CONFIGURED.value());
        }
        return mapper.toEntity(domain);
    }

    protected D toDomain(E entity) {
        if (mapper == null) {
            throw new DatabaseOperationException(DatabaseEnumEntity.MSG_MAPPER_NOT_CONFIGURED.value());
        }
        return mapper.toDomain(entity);
    }

    public Mono<D> save(D domain) {
        DomainValidation.requireNonNull(domain, DatabaseEnumEntity.PARAM_DOMAIN.value());
        return Mono.fromCallable(() -> toEntity(domain))
                .map(this::beforeSave)
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    public Flux<D> saveAll(Flux<D> domainFlux) {
        DomainValidation.requireNonNull(domainFlux, DatabaseEnumEntity.PARAM_DOMAIN_FLUX.value());
        return domainFlux
                .map(this::toEntity)
                .map(this::beforeSave)
                .collectList()
                .flatMapMany(repository::saveAll)
                .map(this::toDomain);
    }

    public Mono<D> findById(UUID id) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        return repository.findById(id).map(this::toDomain);
    }

    public Mono<Boolean> existsById(UUID id) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        return repository.existsById(id);
    }

    public Mono<Long> count() {
        return repository.count();
    }

    public Flux<D> findByExample(D domain) {
        DomainValidation.requireNonNull(domain, DatabaseEnumEntity.PARAM_DOMAIN.value());
        return Mono.fromCallable(() -> Example.of(toEntity(domain)))
                .flatMapMany(repository::findAll)
                .map(this::toDomain);
    }

    public Flux<D> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<PageResponse<D>> findPaginated(D domain, int page, int size, String sortBy, String sortDirection) {
        DomainValidation.requireNonNull(domain, DatabaseEnumEntity.PARAM_DOMAIN.value());
        DomainValidation.requireNonNull(sortBy, DatabaseEnumEntity.PARAM_SORT_BY.value());
        DomainValidation.requireNonNull(sortDirection, DatabaseEnumEntity.PARAM_SORT_DIRECTION.value());

        return Mono.fromCallable(() -> {
            ExampleMatcher matcher = ExampleMatcher.matchingAll()
                    .withIgnoreNullValues()
                    .withIgnoreCase()
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

            return Example.of(toEntity(domain), matcher);
        }).flatMap(example -> {
            Sort sort = sortDirection.equalsIgnoreCase(DatabaseEnumEntity.ORDER_DESC.value())
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Mono<Long> totalElementsMono = repository.count(example);
            Flux<D> contentFlux = repository.findAll(example, sort)
                    .skip((long) page * size)
                    .take(size)
                    .map(this::toDomain);

            return Mono.zip(contentFlux.collectList(), totalElementsMono)
                    .map(tuple -> {
                        long totalElements = tuple.getT2();
                        long totalPages = (long) Math.ceil((double) totalElements / size);
                        return PageResponse.<D>builder()
                                .content(tuple.getT1())
                                .pageNumber(page)
                                .pageSize(size)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .hasPrevious(page > 0)
                                .hasNext(page < totalPages - 1)
                                .isLast(page >= totalPages - 1)
                                .build();
                    });
        });
    }

    public Mono<PageResponse<D>> findPaginated(D domain, int page, int size) {
        return findPaginated(domain, page, size, idFieldName, DatabaseEnumEntity.ORDER_ASC.value());
    }

    public Mono<PageResponse<D>> findPaginated(int page, int size) {
        return repository.count()
                .flatMap(totalElements -> {
                    long totalPages = (long) Math.ceil((double) totalElements / size);
                    return repository.findAll()
                            .skip((long) page * size)
                            .take(size)
                            .map(this::toDomain)
                            .collectList()
                            .map(content -> PageResponse.<D>builder()
                                    .content(content)
                                    .pageNumber(page)
                                    .pageSize(size)
                                    .totalElements(totalElements)
                                    .totalPages(totalPages)
                                    .hasPrevious(page > 0)
                                    .hasNext(page < totalPages - 1)
                                    .isLast(page >= totalPages - 1)
                                    .build());
                });
    }

    public Mono<D> patch(UUID id, D patchDomain) {
        DomainValidation.requireNonNull(id, DatabaseEnumEntity.PARAM_ID.value());
        DomainValidation.requireNonNull(patchDomain, DatabaseEnumEntity.PARAM_PATCH_DOMAIN.value());

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new DatabaseOperationException(DatabaseEnumEntity.MSG_ENTITY_NOT_FOUND_PREFIX.value() + id)))
                .flatMap(existingEntity -> Mono.fromCallable(() -> {
                    E patchEntity = toEntity(patchDomain);
                    copyNonNullProperties(patchEntity, existingEntity);
                    existingEntity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    existingEntity.setNew(false);
                    return existingEntity;
                }))
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    public Mono<Long> countByExample(D domain) {
        DomainValidation.requireNonNull(domain, DatabaseEnumEntity.PARAM_DOMAIN.value());
        return Mono.fromCallable(() -> Example.of(toEntity(domain)))
                .flatMap(repository::count);
    }

    public Mono<Boolean> existsByExample(D domain) {
        DomainValidation.requireNonNull(domain, DatabaseEnumEntity.PARAM_DOMAIN.value());
        return Mono.fromCallable(() -> Example.of(toEntity(domain)))
                .flatMap(repository::exists);
    }

    protected E beforeSave(E entity) {
        DomainValidation.requireNonNull(entity, DatabaseEnumEntity.PARAM_ENTITY.value());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
            entity.setNew(true);
        } else {
            entity.setNew(false);
        }
        entity.setUpdatedAt(now);
        return entity;
    }

    protected void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Stream.of(src.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> !propertyName.equals(DatabaseEnumEntity.FIELD_ID.value()))
                .filter(propertyName -> !propertyName.equals(DatabaseEnumEntity.FIELD_CREATED_AT.value()))
                .filter(propertyName -> !propertyName.equals(DatabaseEnumEntity.FIELD_UPDATED_AT.value()))
                .filter(propertyName -> !propertyName.equals(DatabaseEnumEntity.FIELD_VERSION.value()))
                .filter(propertyName -> src.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }
}
