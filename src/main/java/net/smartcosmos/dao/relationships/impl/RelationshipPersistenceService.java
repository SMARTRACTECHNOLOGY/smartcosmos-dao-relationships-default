package net.smartcosmos.dao.relationships.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.relationships.RelationshipDao;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.SearchSpecifications;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.dto.relationships.RelationshipUpsert;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
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

    @Override
    public RelationshipResponse upsert(String accountUrn, RelationshipUpsert upsertRelationship) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        UUID existingEntityId = getExistingEntityId(accountId, upsertRelationship);

        RelationshipEntity entity = conversionService.convert(upsertRelationship, RelationshipEntity.class);
        entity.setId(existingEntityId);
        entity.setAccountId(accountId);
        entity = persist(entity);

        return conversionService.convert(entity, RelationshipResponse.class);
    }

    @Override
    public Optional<RelationshipResponse> findByUrn(String accountUrn, String urn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        Optional<RelationshipEntity> entity = Optional.empty();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            entity = relationshipRepository.findByAccountIdAndId(accountId, uuid);
        }
        catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, accountUrn);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(),
                RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RelationshipResponse> findSpecific(String accountUrn, String entityReferenceType, String referenceUrn, String relatedEntityReferenceType, String relatedReferenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

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
            log.warn("Illegal URN submitted by account %s: reference URN %s, related reference URN %s", accountUrn, referenceUrn, relatedReferenceUrn);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(),
                RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public List<RelationshipResponse> delete(String accountUrn, String urn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> deleteList = new ArrayList<>();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            deleteList = relationshipRepository.deleteByAccountIdAndId(accountId, uuid);
        } catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, accountUrn);
        }

        return getResponseList(deleteList);
    }

    @Override
    public List<RelationshipResponse> findBetweenEntities(String accountUrn, String entityReferenceType, String referenceUrn, String relatedEntityReferenceType, String relatedReferenceUrn) {
        return null;
    }

    @Override
    public List<RelationshipResponse> findByType(String accountUrn, String entityReferenceType, String referenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndType(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s", accountUrn, referenceUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findByTypeReverse(String accountUrn, String relatedEntityReferenceType, String relatedReferenceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndRelatedEntityReferenceTypeAndRelatedReferenceIdAndType(
                accountId,
                relatedEntityReferenceType,
                UuidUtil.getUuidFromUrn(relatedReferenceUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: related reference URN %s", accountUrn, relatedReferenceUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findAll(String accountUrn, String entityReferenceType, String referenceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                accountId,
                entityReferenceType,
                UuidUtil.getUuidFromUrn(referenceUrn));
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: reference URN %s", accountUrn, referenceUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findAllReflexive(String accountUrn, String entityReferenceType, String referenceUrn) {

        Specification<RelationshipEntity> accountUrnSpecification = null;
        if (StringUtils.isNotBlank(accountUrn)) {
            UUID accountUuid = UuidUtil.getUuidFromAccountUrn(accountUrn);
            accountUrnSpecification = searchSpecifications.matchUuid(accountUuid, "accountId");
        }

        Iterable<RelationshipEntity> returnedValues = relationshipRepository.findAll(Specifications.where(accountUrnSpecification));

        // TODO: Add Specification for reflexive query

        List<RelationshipResponse> convertedList = new ArrayList<>();
        for (RelationshipEntity entity: returnedValues) {
            convertedList.add(conversionService.convert(entity, RelationshipResponse.class));
        }
        return convertedList;
    }


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

    private UUID getExistingEntityId(UUID accountId, RelationshipUpsert upsertRelationship) {
        Optional<RelationshipEntity> existingEntity = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
            accountId,
            upsertRelationship.getEntityReferenceType(),
            UuidUtil.getUuidFromUrn(upsertRelationship.getReferenceUrn()),
            upsertRelationship.getType(),
            upsertRelationship.getRelatedEntityReferenceType(),
            UuidUtil.getUuidFromUrn(upsertRelationship.getRelatedReferenceUrn()));

        return (existingEntity.isPresent() ? existingEntity.get().getId() : null);
    }

    private List<RelationshipResponse> getResponseList(List<RelationshipEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, RelationshipResponse.class))
            .collect(Collectors.toList());
    }
}
