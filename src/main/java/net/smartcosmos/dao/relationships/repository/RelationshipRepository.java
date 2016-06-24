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
    Optional<RelationshipEntity> findByTenantIdAndId(UUID accountId, UUID id);

    Optional<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndRelationshipTypeAndTargetTypeAndTargetId(
        UUID tenantId,
        String sourceType,
        UUID sourceId,
        String relationshipType,
        String relatedEntityReferenceType,
        UUID relatedReferenceId);

    List<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(UUID tenantId, String sourceType, UUID sourceId, String relationshipType);

    List<RelationshipEntity> findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(UUID tenantId, String relatedEntityReferenceType, UUID relatedReferenceId, String relationshipType);

    List<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(UUID tenantId, String sourceType, UUID sourceId, String targetType, UUID targetId);

    List<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceId(UUID tenantId, String sourceType, UUID sourceId);

    @Transactional
    List<RelationshipEntity> deleteByTenantIdAndId(UUID accountId, UUID id);
}
