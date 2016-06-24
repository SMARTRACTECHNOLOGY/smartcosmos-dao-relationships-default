package net.smartcosmos.dao.relationships.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.relationships.RelationshipDao;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.SearchSpecifications;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.BooleanUtils;
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

    @Override
    public Optional<RelationshipResponse> create(String accountUrn, RelationshipCreate createRelationship) {

        if (alreadyExists(accountUrn, createRelationship)) {
            return Optional.empty();
        }
        UUID tenantId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        RelationshipEntity entity = conversionService.convert(createRelationship, RelationshipEntity.class);
        entity.setTenantId(tenantId);
        entity = persist(entity);

        return Optional.ofNullable(conversionService.convert(entity, RelationshipResponse.class));
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
    public Optional<RelationshipResponse> findSpecific(String accountUrn, String sourceType, String sourceUrn, String targetType, String targetUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        Optional<RelationshipEntity> entity = Optional.empty();
        try {
            entity = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn),
                type,
                targetType,
                UuidUtil.getUuidFromUrn(targetUrn));
        } catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted by account %s: source URN %s, target URN %s", accountUrn, sourceUrn, targetUrn);
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
            // empty list will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, accountUrn);
        }

        return getResponseList(deleteList);
    }

    @Override
    public List<RelationshipResponse> findBetweenEntities(String accountUrn, String sourceType, String sourceUrn, String targetType, String targetUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn),
                targetType,
                UuidUtil.getUuidFromUrn(targetUrn));
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted by account %s: source URN %s, target URN %s", accountUrn, sourceUrn, targetUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findByType(String accountUrn, String sourceType, String sourceUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndType(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty list will be returned anyway
            log.warn("Illegal URN submitted by account %s: source URN %s", accountUrn, sourceUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findByTypeReverse(String accountUrn, String targetType, String targetUrn, String type) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndRelatedEntityReferenceTypeAndRelatedReferenceIdAndType(
                accountId,
                targetType,
                UuidUtil.getUuidFromUrn(targetUrn),
                type);
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: target URN %s", accountUrn, targetUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findAll(String accountUrn, String sourceType, String sourceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipEntity> entityList = new ArrayList<>();
        try {
            entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn));
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: source URN %s", accountUrn, sourceUrn);
        }

        return getResponseList(entityList);
    }

    @Override
    public List<RelationshipResponse> findAll(String accountUrn, String sourceType, String sourceUrn, Boolean checkReciprocal) {

        List<RelationshipResponse> responseList = new ArrayList<>();

        if (BooleanUtils.isTrue(checkReciprocal)) {
            UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

            try {
                List<RelationshipEntity> entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                    accountId,
                    sourceType,
                    UuidUtil.getUuidFromUrn(sourceUrn));

                return entityList.stream()
                    .map(o -> convertAndIncludeReciprocalFlag(accountId, o))
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // empty list will be returned anyway
                log.warn("Illegal URN submitted by account %s: source URN %s", accountUrn, sourceUrn);
            }
        } else {
            responseList = findAllSymmetric(accountUrn, sourceType, sourceUrn);
        }

        return responseList;
    }

    @Override
    public List<RelationshipResponse> findAllSymmetric(String accountUrn, String sourceType, String sourceUrn) {

        UUID accountId = UuidUtil.getUuidFromAccountUrn(accountUrn);

        List<RelationshipResponse> resultList = new ArrayList<>();
        try {
            List<RelationshipEntity> entityList = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceId(
                accountId,
                sourceType,
                UuidUtil.getUuidFromUrn(sourceUrn));

            for (RelationshipEntity entity : entityList) {
                Optional<RelationshipEntity> reciprocal = findReciprocalRelationshipEntity(accountId, entity);
                if (reciprocal.isPresent()) {
                    resultList.add(conversionService.convert(entity, RelationshipResponse.class));
                    resultList.add(conversionService.convert(reciprocal.get(), RelationshipResponse.class));
                }
            }
        } catch (IllegalArgumentException e) {
            // empty will be returned anyway
            log.warn("Illegal URN submitted by account %s: source URN %s", accountUrn, sourceUrn);
        }

        return resultList;
    }

    private RelationshipResponse convertAndIncludeReciprocalFlag(UUID accountId, RelationshipEntity entity) {
        RelationshipResponse response = conversionService.convert(entity, RelationshipResponse.class);
        response.setReciprocal(findReciprocalRelationshipEntity(accountId, entity).isPresent());

        return response;
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

    private UUID getExistingEntityId(UUID accountId, RelationshipCreate createRelationship) {
        Optional<RelationshipEntity> existingEntity = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
            accountId,
            createRelationship.getSource().getType(),
            UuidUtil.getUuidFromUrn(createRelationship.getSource().getUrn()),
            createRelationship.getRelationshipType(),
            createRelationship.getTarget().getType(),
            UuidUtil.getUuidFromUrn(createRelationship.getTarget().getUrn()));

        return (existingEntity.isPresent() ? existingEntity.get().getId() : null);
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

    private List<RelationshipResponse> getResponseList(List<RelationshipEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, RelationshipResponse.class))
            .collect(Collectors.toList());
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

}
