package net.smartcosmos.dao.relationships.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;

public interface RelationshipRepository extends
                                        JpaRepository<RelationshipEntity, UUID>,
                                        QueryByExampleExecutor<RelationshipEntity>,
                                        JpaSpecificationExecutor<RelationshipEntity>,
                                        PagingAndSortingRepository<RelationshipEntity, UUID> {

    Optional<RelationshipEntity> findByTenantIdAndId(
        UUID accountId, UUID id);

    Optional<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndRelationshipTypeAndTargetTypeAndTargetId(
        UUID tenantId,
        String sourceType,
        UUID sourceId,
        String relationshipType,
        String targetType,
        UUID targetId);

    Page<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(
        UUID tenantId,
        String sourceType,
        UUID sourceId,
        String relationshipType,
        Pageable pageable);

    Page<RelationshipEntity> findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(
        UUID tenantId,
        String targetType,
        UUID targetId,
        String relationshipType,
        Pageable pageable);

    Page<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
        UUID tenantId,
        String sourceType,
        UUID sourceId,
        String targetType,
        UUID targetId,
        Pageable pageable);

    Page<RelationshipEntity> findByTenantIdAndSourceTypeAndSourceId(
        UUID tenantId,
        String sourceType,
        UUID sourceId,
        Pageable pageable);

    Page<RelationshipEntity> findByTenantIdAndTargetTypeAndTargetId(
        UUID tenantId,
        String targetType,
        UUID targetId,
        Pageable pageable);

    @Transactional
    List<RelationshipEntity> deleteByTenantIdAndId(UUID accountId, UUID id);
}
