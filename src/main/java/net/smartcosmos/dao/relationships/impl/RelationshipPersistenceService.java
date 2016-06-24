package net.smartcosmos.dao.relationships.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.relationships.RelationshipDao;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.SearchSpecifications;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RelationshipPersistenceService implements RelationshipDao {

    private final RelationshipRepository relationshipRepository;
    private final ConversionService conversionService;
    private final SearchSpecifications<RelationshipEntity> searchSpecifications = new SearchSpecifications<RelationshipEntity>();

    @Autowired
    public RelationshipPersistenceService(RelationshipRepository RelationshipRepository,
            ConversionService conversionService) {
        this.relationshipRepository = RelationshipRepository;
        this.conversionService = conversionService;
    }

    // region Create

    @Override
    public Optional<RelationshipResponse> create(String tenantUrn, RelationshipCreate createRelationship) throws ConstraintViolationException {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // endregion

    // region Delete

    @Override
    public List<RelationshipResponse> delete(String tenantUrn, String urn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipEntity> deleteList = new ArrayList<>();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            deleteList = relationshipRepository.deleteByAccountIdAndId(accountId, uuid);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, tenantUrn);
        }

        return convert(deleteList);
    }

    // endregion

    // region Find specific / by URN

    @Override
    public Optional<RelationshipResponse> findByUrn(String tenantUrn, String urn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        Optional<RelationshipEntity> entity = Optional.empty();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            entity = relationshipRepository.findByAccountIdAndId(accountId, uuid);
        }
        catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, tenantUrn);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(),
                RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RelationshipResponse> findSpecific(String tenantUrn, String entityReferenceType, String referenceUrn, String relatedEntityReferenceType, String relatedReferenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        Optional<RelationshipEntity> entity = Optional.empty();
        try {
            entity = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn),
                type,
                relatedEntityReferenceType,
                UuidUtil.getUuidFromUrn(relatedReferenceUrn));
        } catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s, related reference URN %s", tenantUrn, referenceUrn, relatedReferenceUrn);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(),
                RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    // endregion

    // region Find Between

    @Override
    public Page<RelationshipResponse> findBetweenEntities(String tenantUrn, String sourceType, String sourceUrn, String targetType, String targetUrn, Integer page, Integer size) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // endregion

    // region Find By Type

    @Override
    public Page<RelationshipResponse> findByTypeForSource(String tenantUrn, String sourceType, String sourceUrn, String relationshipType, Integer page, Integer size) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<RelationshipResponse> findByTypeForTarget(String tenantUrn, String targetType, String targetUrn, String relationshipType, Integer page, Integer size) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // endregion

    // region Find All

    @Override
    public Page<RelationshipResponse> findAllForSource(String tenantUrn, String sourceType, String sourceUrn, Integer page, Integer size) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<RelationshipResponse> findAllForTarget(String tenantUrn, String targetType, String targetUrn, Integer page, Integer size) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // endregion

    // region v2 implementations

    @Deprecated
    public List<RelationshipResponse> findBetweenEntities(String tenantUrn, String entityReferenceType, String referenceUrn, String relatedEntityReferenceType, String relatedReferenceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn),
                relatedEntityReferenceType,
                UuidUtil.getUuidFromUrn(relatedReferenceUrn));
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s, related reference URN %s", tenantUrn, referenceUrn, relatedReferenceUrn);
        }

        return convert(entityList);
    }

    @Deprecated
    public List<RelationshipResponse> findByType(String tenantUrn, String entityReferenceType, String referenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndType(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s", tenantUrn, referenceUrn);
        }

        return convert(entityList);
    }

    @Deprecated
    public List<RelationshipResponse> findByTypeReverse(String tenantUrn, String relatedEntityReferenceType, String relatedReferenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndRelatedEntityReferenceTypeAndRelatedReferenceIdAndType(
                accountId,
                relatedEntityReferenceType,
                UuidUtil.getUuidFromUrn(relatedReferenceUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: related reference URN %s", tenantUrn, relatedReferenceUrn);
        }

        return convert(entityList);
    }

    @Deprecated
    public List<RelationshipResponse> findAll(String tenantUrn, String entityReferenceType, String referenceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn));
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s", tenantUrn, referenceUrn);
        }

        return convert(entityList);
    }

    @Deprecated
    public List<RelationshipResponse> findAll(String tenantUrn, String entityReferenceType, String referenceUrn, Boolean checkReciprocal) {

        return findAllSymmetric(tenantUrn, entityReferenceType, referenceUrn);
    }

    @Deprecated
    public List<RelationshipResponse> findAllSymmetric(String tenantUrn, String entityReferenceType, String referenceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(tenantUrn);

        List<RelationshipResponse> resultList = new ArrayList<>();
        try {
            List<RelationshipEntity> entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn));

            for (RelationshipEntity entity : entityList) {
                Optional<RelationshipEntity> reciprocal = findReciprocalRelationshipEntity(accountId, entity);
                if (reciprocal.isPresent()) {
                    resultList.add(conversionService.convert(entity, RelationshipResponse.class));
                    resultList.add(conversionService.convert(reciprocal.get(), RelationshipResponse.class));
                }
            }
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s", tenantUrn, referenceUrn);
        }

        return resultList;
    }

    // endregion

    // region Helper Methods

    /**
     * Saves an object entity in an {@link RelationshipRepository}.
     *
     * @param relationshipEntity the object entity to persist
     * @return the persisted object entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
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

    private Optional<RelationshipEntity> findReciprocalRelationshipEntity(UUID accountId, RelationshipEntity entity) {
        return relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
            accountId,
            entity.getRelatedEntityReferenceType(),
            entity.getRelatedReferenceId(),
            entity.getType(),
            entity.getEntityReferenceType(),
            entity.getReferenceId());
    }

    private List<RelationshipResponse> convert(List<RelationshipEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, RelationshipResponse.class))
            .collect(Collectors.toList());
    }

    // endregion
}
