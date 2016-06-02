package net.smartcosmos.dao.relationships.repository;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RelationshipRepository extends
        JpaRepository<RelationshipEntity, UUID>,
        QueryByExampleExecutor<RelationshipEntity>,
        JpaSpecificationExecutor<RelationshipEntity>
{
    Optional<RelationshipEntity> findByAccountIdAndId(UUID accountId, UUID id);

    Optional<RelationshipEntity> findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
        UUID accountId,
        String entityReferenceType,
        UUID referenceId,
        String type,
        String relatedEntityReferenceType,
        UUID relatedReferenceId);

    List<RelationshipEntity> findByAccountIdAndEntityReferenceTypeAndReferenceUrnAndType(UUID accountId, String entityReferenceType, UUID referenceId, String type);

    List<RelationshipEntity> findByAccountIdAndRelatedEntityReferenceTypeAndRelatedReferenceUrnAndType(UUID accountId, String relatedEntityReferenceType, UUID relatedReferenceId, String type);

    @Transactional
    List<RelationshipEntity> deleteByAccountIdAndId(UUID accountId, UUID id);
}
