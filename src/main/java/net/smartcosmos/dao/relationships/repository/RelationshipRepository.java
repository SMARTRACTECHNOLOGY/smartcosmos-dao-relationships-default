package net.smartcosmos.dao.relationships.repository;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RelationshipRepository extends
        JpaRepository<RelationshipEntity, UUID>,
        QueryByExampleExecutor<RelationshipEntity>,
        JpaSpecificationExecutor<RelationshipEntity>
{
    Optional<RelationshipEntity> findByAccountIdAndId(UUID accountId, UUID id);
}
