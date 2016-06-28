package net.smartcosmos.dao.relationships.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import net.smartcosmos.dao.relationships.RelationshipDao;
import net.smartcosmos.dao.relationships.SortOrder;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.RelationshipPersistenceUtil;
import net.smartcosmos.dao.relationships.util.SearchSpecifications;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipResponse;

@Slf4j
@Service
public class RelationshipPersistenceService implements RelationshipDao {

    private final RelationshipRepository relationshipRepository;
    private final ConversionService conversionService;
    private final SearchSpecifications<RelationshipEntity> searchSpecifications = new SearchSpecifications<RelationshipEntity>();

    @Autowired
    public RelationshipPersistenceService(
        RelationshipRepository RelationshipRepository,
        ConversionService conversionService) {
        this.relationshipRepository = RelationshipRepository;
        this.conversionService = conversionService;
    }

    /**
     * Create a relationship.
     *
     * @param tenantUrn the tenant URN
     * @param createRelationship the relationship to create
     * @return
     */
    @Override
    public Optional<RelationshipResponse> create(String tenantUrn, RelationshipCreate createRelationship) {

        if (alreadyExists(tenantUrn, createRelationship)) {
            return Optional.empty();
        }

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            RelationshipEntity entity = conversionService.convert(createRelationship, RelationshipEntity.class);
            entity.setTenantId(tenantId);
            entity = persist(entity);
            return Optional.ofNullable(conversionService.convert(entity, RelationshipResponse.class));

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("create failed, tenant: '%s', cause: %s", tenantUrn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Delete a relationship.
     *
     * @param tenantUrn the tenant URN
     * @param urn the relationship's system-assigned URN
     * @return
     */
    @Override
    public List<RelationshipResponse> delete(String tenantUrn, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            return convertList(relationshipRepository.deleteByTenantIdAndId(tenantId, uuid));

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findByUrn failed, tenant: '%s', urn: '%s', cause: %s", tenantUrn, urn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find a relationship by its URN.
     *
     * @param tenantUrn the tenant URN
     * @param urn the relationship's system-assigned URN
     * @return
     */
    @Override
    public Optional<RelationshipResponse> findByUrn(String tenantUrn, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            Optional<RelationshipEntity> entity = relationshipRepository.findByTenantIdAndId(tenantId, uuid);
            if (entity.isPresent()) {
                final RelationshipResponse response = conversionService.convert(entity.get(), RelationshipResponse.class);
                return Optional.ofNullable(response);
            }
            return Optional.empty();

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findByUrn failed, tenant: '%s', urn: '%s', cause: %s", tenantUrn, urn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find a particular relationship based on its source, target, and relationshipType.
     *
     * @param tenantUrn the tenant URN
     * @param sourceType the source entity type
     * @param sourceUrn the source entity's system-assigned URN
     * @param targetType the target entity type
     * @param targetUrn the target entity's system-assigned URN
     * @param relationshipType the relationship type
     * @return
     */
    @Override
    public Optional<RelationshipResponse> findSpecific(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        String targetType,
        String targetUrn,
        String relationshipType) {

        try {
            UUID accountId = UuidUtil.getUuidFromUrn(tenantUrn);
            Optional<RelationshipEntity> entity = relationshipRepository
                .findByTenantIdAndSourceTypeAndSourceIdAndRelationshipTypeAndTargetTypeAndTargetId(
                    accountId,
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    relationshipType,
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn));

            if (entity.isPresent()) {
                final RelationshipResponse response = conversionService.convert(entity.get(), RelationshipResponse.class);
                return Optional.ofNullable(response);
            }
            return Optional.empty();

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findSpecific failed, tenant: '%s', sourceType: '%s', sourceUrn: '%s', targetType: '%s', targetUrn: '%s', " +
                                       "relationshipType: '%s', cause: %s", tenantUrn, sourceType, sourceUrn, targetType, targetUrn, relationshipType,
                                       e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }

    }

    /**
     * Find all relationships between a source and a target.
     *
     * @param tenantUrn the tenant URN
     * @param sourceType the source entity type
     * @param sourceUrn the source entity's system-assigned URN
     * @param targetType the target entity type
     * @param targetUrn the target entity's system-assigned URN
     * @param page the number of the results page
     * @param size the size of a results page
     * @param sortOrder order to sort the result, can be {@code ASC} or {@code DESC}
     * @param sortBy name of the field to sort by
     * @return
     */
    public Page<RelationshipResponse> findBetweenEntities(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        String targetType,
        String targetUrn,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        try {
            Pageable pageable = buildPageable(page, size, sortOrder, sortBy);
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn),
                    pageable);

            return conversionService.convert(entityPage, RelationshipPersistenceUtil.emptyPage().getClass());

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findBetweenEntities failed, tenant: '%s', sourceType: '%s', sourceUrn: '%s', targetType: '%s', targetUrn: " +
                                       "'%s', cause: %s", tenantUrn, sourceType, sourceUrn, targetType, targetUrn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find all relationships of a particular relationshipType with a particular source.
     *
     * @param tenantUrn the tenant URN
     * @param sourceType the source entity type
     * @param sourceUrn the source entity's system-assigned URN
     * @param relationshipType the relationship type
     * @param page the number of the results page
     * @param size the size of a results page
     * @param sortOrder order to sort the result, can be {@code ASC} or {@code DESC}
     * @param sortBy name of the field to sort by
     * @return
     */
    @Override
    public Page<RelationshipResponse> findByTypeForSource(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        String relationshipType,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    relationshipType,
                    buildPageable(page, size, sortOrder, sortBy));

            return conversionService.convert(entityPage, RelationshipPersistenceUtil.emptyPage().getClass());

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findByTypeForSource failed, tenant: '%s', sourceType: '%s', sourceUrn: '%s', relationshipType: '%s'," +
                                       "cause: '%s'", tenantUrn, sourceType, sourceUrn, relationshipType, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find all relationships of a particular relationshipType with a particular target.
     *
     * @param tenantUrn the tenant URN
     * @param targetType the target entity type
     * @param targetUrn the target entity's system-assigned URN
     * @param relationshipType the relationship type
     * @param page the number of the results page
     * @param size the size of a results page
     * @param sortOrder
     * @param sortBy
     * @return
     */
    @Override
    public Page<RelationshipResponse> findByTypeForTarget(
        String tenantUrn,
        String targetType,
        String targetUrn,
        String relationshipType,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn),
                    relationshipType,
                    buildPageable(page, size, sortOrder, sortBy));

            return conversionService.convert(entityPage, RelationshipPersistenceUtil.emptyPage().getClass());

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findByTypeForSource failed, tenant: '%s', sourceType: '%s', sourceUrn: '%s', relationshipType: '%s'," +
                                       "cause: '%s'", tenantUrn, targetType, targetUrn, relationshipType, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find all relationships with a particular source.
     *
     * @param tenantUrn the tenant URN
     * @param sourceType the source entity type
     * @param sourceUrn the source entity's system-assigned URN
     * @param page the number of the results page
     * @param size the size of a results page
     * @param sortOrder
     * @param sortBy
     * @return
     */
    @Override
    public Page<RelationshipResponse> findAllForSource(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceId(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    buildPageable(page, size, sortOrder, sortBy));

            return conversionService.convert(entityPage, RelationshipPersistenceUtil.emptyPage().getClass());

        } catch (IllegalArgumentException e) {
            String msg = String.format("findByTypeForSource failed, tenant: '%s', sourceType: '%s', sourceUrn: '%s', " +
                                       "cause: '%s'", tenantUrn, sourceType, sourceUrn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Find all relationships with a particular target.
     *
     * @param tenantUrn the tenant URN
     * @param targetType the target entity type
     * @param targetUrn the target entity's system-assigned URN
     * @param page the number of the results page
     * @param size the size of a results page
     * @param sortOrder
     * @param sortBy
     * @return
     */
    @Override
    public Page<RelationshipResponse> findAllForTarget(
        String tenantUrn,
        String targetType,
        String targetUrn,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndTargetTypeAndTargetId(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn),
                    buildPageable(page, size, sortOrder, sortBy));

            return conversionService.convert(entityPage, RelationshipPersistenceUtil.emptyPage().getClass());

        } catch (IllegalArgumentException | ConversionException e) {
            String msg = String.format("findByTypeForSource failed, tenant: '%s', targetType: '%s', targetUrn: '%s', cause: '%s'", tenantUrn,
                                       targetType, targetUrn, e.toString());
            log.error(msg);
            log.debug(msg, e);
            throw e;
        }
    }

    /**
     * Saves an object entity in an {@link RelationshipRepository}.
     *
     * @param relationshipEntity the object entity to persist
     * @return the persisted object entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException         if the transaction fails because of something else
     */
    private RelationshipEntity persist(RelationshipEntity relationshipEntity) throws ConstraintViolationException, TransactionException {
        try {
            return relationshipRepository.save(relationshipEntity);
        } catch (TransactionException e) {

            // we expect constraint violations to be the root cause for exceptions here,
            // so we throw this particular exception back to the caller
            if (ExceptionUtils.getRootCause(e) instanceof ConstraintViolationException) {
                throw (ConstraintViolationException) ExceptionUtils.getRootCause(e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Converts a list of RelationshipEntity to a list of RelationshipResonse.
     *
     * @param entityList
     * @return
     */
    private List<RelationshipResponse> convertList(List<RelationshipEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, RelationshipResponse.class))
            .collect(Collectors.toList());
    }

    private Pageable buildPageable(Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        Sort.Direction direction = Sort.DEFAULT_DIRECTION; // TODO default value to service config
        if (sortOrder != null) {
            direction = RelationshipPersistenceUtil.getSortDirection(sortOrder);
        }
        if (sortBy == null) {
            sortBy = RelationshipPersistenceUtil.getSortByFieldName("created"); // TODO default value to service config
        }
        if (page == null) {
            page = 0; // TODO default value to service config
        }
        if (size == null) {
            size = 20; // TODO default value to service config
        }
        return new PageRequest(page, size, direction, sortBy);
    }

    /**
     * Uses the public findSpecific() method to determine whheter a relationship already exists.
     *
     * @param accountUrn
     * @param createRelationship
     * @return
     */
    private boolean alreadyExists(String accountUrn, RelationshipCreate createRelationship) {

        Optional<RelationshipResponse> existing = findSpecific(
            accountUrn,
            createRelationship.getSource().getType(),
            createRelationship.getSource().getUrn(),
            createRelationship.getTarget().getType(),
            createRelationship.getTarget().getUrn(),
            createRelationship.getRelationshipType());

        if (existing.isPresent()) {
            return true;
        }
        return false;
    }
}
