package net.smartcosmos.dao.relationships.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.util.WeakReferenceMonitor;

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

    // region Create
    @Override
    public Optional<RelationshipResponse> create(String tenantUrn, RelationshipCreate createRelationship) {

        if (alreadyExists(tenantUrn, createRelationship)) {
            return Optional.empty();
        }
        RelationshipEntity entity = null;
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            entity = conversionService.convert(createRelationship, RelationshipEntity.class);
            entity.setTenantId(tenantId);
            entity = persist(entity);

        } catch (Exception e) {
            exceptionLogger("RelationshipPersistenceService:create", e, tenantUrn, createRelationship);
            throw e;
        }
        return Optional.ofNullable(conversionService.convert(entity, RelationshipResponse.class));
    }
    //endregion

    // region delete
    @Override
    public List<RelationshipResponse> delete(String tenantUrn, String urn) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<RelationshipEntity> deleteList = new ArrayList<>();

        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            deleteList = relationshipRepository.deleteByTenantIdAndId(tenantId, uuid);

        } catch (Exception e) {
            exceptionLogger("RelationshipPersistenceService:delete", e, "tenantUrn: " + tenantUrn, "urn: " + urn);
        }
        return convertList(deleteList);
    }
    // endregion

    // region Find specific / by URN
    @Override
    public Optional<RelationshipResponse> findByUrn(String tenantUrn, String urn) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        Optional<RelationshipEntity> entity = Optional.empty();

        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            entity = relationshipRepository.findByTenantIdAndId(tenantId, uuid);
        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findByUrn", e, "tenantUrn: " + tenantUrn, "urn: " + urn);
        }
        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(), RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RelationshipResponse> findSpecific(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        String targetType,
        String targetUrn,
        String relationshipType) {

        UUID accountId = UuidUtil.getUuidFromUrn(tenantUrn);
        Optional<RelationshipEntity> entity = Optional.empty();

        try {
            entity = relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipTypeAndTargetTypeAndTargetId(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn),
                relationshipType,
                targetType,
                UuidUtil.getUuidFromUrn(targetUrn));
        } catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            exceptionLogger("RelationshipPersistenceService:findSpecific", e, sourceType, sourceUrn, relationshipType, targetType);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(), RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }
    // endregion

    // region Find Between Entities
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

        Pageable pageable = buildPageable(page, size, sortOrder, sortBy);

        Page<RelationshipResponse> responsePage = RelationshipPersistenceUtil.emptyPage();

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
                UuidUtil.getUuidFromUrn(tenantUrn),
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn),
                targetType,
                UuidUtil.getUuidFromUrn(targetUrn),
                pageable);

            return conversionService.convert(entityPage, responsePage.getClass());

        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findBetweenEntities", e, tenantUrn, sourceType, sourceUrn, targetType, targetUrn);
        }
        return responsePage;
    }

    // endregion

    // region Find By Type
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

        Pageable pageable = buildPageable(page, size, sortOrder, sortBy);

        Page<RelationshipResponse> responsePage = RelationshipPersistenceUtil.emptyPage();

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    relationshipType,
                    pageable);

            return conversionService.convert(entityPage, responsePage.getClass());

        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findByTypeForSource", e, tenantUrn, sourceType, sourceUrn, relationshipType);
        }
        return responsePage;
    }


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

        Pageable pageable = buildPageable(page, size, sortOrder, sortBy);

        Page<RelationshipResponse> responsePage = RelationshipPersistenceUtil.emptyPage();

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn),
                    relationshipType,
                    pageable);

            return conversionService.convert(entityPage, responsePage.getClass());

        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findByTypeForTarget", e, tenantUrn, targetType, targetUrn, relationshipType);
        }
        return responsePage;
    }
    // endregion

    // region Find All
    @Override
    public Page<RelationshipResponse> findAllForSource(
        String tenantUrn,
        String sourceType,
        String sourceUrn,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        Pageable pageable = buildPageable(page, size, sortOrder, sortBy);

        Page<RelationshipResponse> responsePage = RelationshipPersistenceUtil.emptyPage();

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndSourceTypeAndSourceId(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn),
                    pageable);

            return conversionService.convert(entityPage, responsePage.getClass());

        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findAllForSource", e, tenantUrn, sourceType, sourceUrn);
        }
        return responsePage;
    }

    @Override
    public Page<RelationshipResponse> findAllForTarget(
        String tenantUrn,
        String targetType,
        String targetUrn,
        Integer page,
        Integer size,
        SortOrder sortOrder,
        String sortBy) {

        Pageable pageable = buildPageable(page, size, sortOrder, sortBy);

        Page<RelationshipResponse> responsePage = RelationshipPersistenceUtil.emptyPage();

        try {
            org.springframework.data.domain.Page<RelationshipEntity> entityPage =
                relationshipRepository.findByTenantIdAndTargetTypeAndTargetId(
                    UuidUtil.getUuidFromUrn(tenantUrn),
                    targetType,
                    UuidUtil.getUuidFromUrn(targetUrn),
                    pageable);

            return conversionService.convert(entityPage, responsePage.getClass());

        } catch (IllegalArgumentException e) {
            exceptionLogger("RelationshipPersistenceService:findAllForSource", e, tenantUrn, targetType, targetUrn);
        }
        return responsePage;
    }
    // endregion

    // region Helper Methods
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

    private void exceptionLogger(String methodName, Exception e, Object... args) {

        StringBuilder messageBuilder = new StringBuilder().append("Method: " + methodName + " " + e.getClass().getSimpleName() + " Root Cause: " +
                                                                  e.getCause() + " ");
        for (Object arg : args) {
            messageBuilder.append(arg + " ");
        }
        log.warn(messageBuilder.toString());
    }

}
