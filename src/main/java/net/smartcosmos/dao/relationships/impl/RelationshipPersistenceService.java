package net.smartcosmos.dao.relationships.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.relationships.RelationshipDao;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.SearchSpecifications;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipLookupSpecific;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.UUID;


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
    public RelationshipResponse create(String accountUrn, RelationshipCreate createRelationship) {

        RelationshipEntity entity = conversionService.convert(createRelationship, RelationshipEntity.class);
        entity = persist(entity);

        return conversionService.convert(entity, RelationshipResponse.class);
    }

    @Override
    public Optional<RelationshipResponse> findByUrn(String accountUrn, String urn) {

        Optional<RelationshipEntity> entity = Optional.empty();
        try {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            entity = relationshipRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), uuid);
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
    public Optional<RelationshipResponse> findSpecific(
            String accountUrn,
            RelationshipLookupSpecific relationshipLookupSpecific) throws ConstraintViolationException {

        Optional<RelationshipEntity> entity = Optional.empty();

        try {
            entity = relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                UuidUtil.getUuidFromAccountUrn(accountUrn),
                relationshipLookupSpecific.getEntityReferenceType(),
                UuidUtil.getUuidFromUrn(relationshipLookupSpecific.getReferenceUrn()),
                relationshipLookupSpecific.getType(),
                relationshipLookupSpecific.getRelatedEntityReferenceType(),
                UuidUtil.getUuidFromUrn(relationshipLookupSpecific.getRelatedReferenceUrn()));
        } catch (IllegalArgumentException e) {
            // Optional.empty() will be returned anyway
            log.warn("Illegal relationshipLookupSpecific submitted by account %s", accountUrn);
        }

        if (entity.isPresent()) {
            final RelationshipResponse response = conversionService.convert(entity.get(),
                RelationshipResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String accountUrn, String urn) throws IllegalArgumentException {
        Optional<RelationshipEntity> entity;
        UUID uuid = UuidUtil.getUuidFromUrn(urn);
        entity = relationshipRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), uuid);
        if (!entity.isPresent()) throw new IllegalArgumentException("Illegal URN submitted: " + urn);
        relationshipRepository.delete(entity.get().getId());
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
}
