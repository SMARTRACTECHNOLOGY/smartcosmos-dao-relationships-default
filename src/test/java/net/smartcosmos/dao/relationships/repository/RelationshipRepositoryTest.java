package net.smartcosmos.dao.relationships.repository;

import net.smartcosmos.dao.relationships.RelationshipPersistenceConfig;
import net.smartcosmos.dao.relationships.RelationshipPersistenceTestApplication;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.util.UuidUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 *
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.tenantId
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {
    RelationshipPersistenceTestApplication.class,
    RelationshipPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class RelationshipRepositoryTest {

    final String TEST_REFERENCE_TYPE = "Thing";
    final String TEST_RELATIONSHIP_TYPE = "Contains";
    final String TEST_MONIKER = "Moniker";

    @Autowired
    RelationshipRepository relationshipRepository;
    final UUID tenantId = UUID.randomUUID();
    private UUID id;
    private UUID referenceId;
    private UUID relatedReferenceId;

    @Before
    public void setUp() throws Exception {
        referenceId = UuidUtil.getNewUuid();
        relatedReferenceId = UuidUtil.getNewUuid();

        RelationshipEntity entity = RelationshipEntity.builder()
                .tenantId(tenantId)
                .sourceType(TEST_REFERENCE_TYPE)
                .sourceId(referenceId)
                .relationshipType(TEST_RELATIONSHIP_TYPE)
                .targetType(TEST_REFERENCE_TYPE)
                .targetId(relatedReferenceId)
                .build();

        entity = relationshipRepository.save(entity);
        id = entity.getId();
    }

    @After
    public void delete() throws Exception {
        relationshipRepository.deleteAll();
    }

    @Test
    public void findByTenantIdAndId() throws Exception {
        assertTrue(this.relationshipRepository.findByTenantIdAndId(tenantId, id).isPresent());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId()
            throws Exception {

        assertTrue(this.relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipTypeAndTargetTypeAndTargetId(
                tenantId,
                TEST_REFERENCE_TYPE,
                referenceId,
                TEST_RELATIONSHIP_TYPE,
                TEST_REFERENCE_TYPE,
                relatedReferenceId)
            .isPresent());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceIdAndType() {

        List<RelationshipEntity> entityList = relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId,
            TEST_RELATIONSHIP_TYPE);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        RelationshipEntity entity = entityList.get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityRelatedReferenceTypeAndRelatedReferenceIdAndType() {

        List<RelationshipEntity> entityList = relationshipRepository.findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(
            tenantId,
            TEST_REFERENCE_TYPE,
            relatedReferenceId,
            TEST_RELATIONSHIP_TYPE);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        RelationshipEntity entity = entityList.get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceIdAndRelatedEntityReferenceTypeAndRelatedEntityReferenceId() {

        List<RelationshipEntity> entityList = relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId,
            TEST_REFERENCE_TYPE,
            relatedReferenceId);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        RelationshipEntity entity = entityList.get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceId() {

        List<RelationshipEntity> entityList = relationshipRepository.findByTenantIdAndSourceTypeAndSourceId(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId);

        assertFalse(entityList.isEmpty());
        assertEquals(1, entityList.size());

        RelationshipEntity entity = entityList.get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void deleteByTenantIdAndId() {
        List<RelationshipEntity> deleteList = relationshipRepository.deleteByTenantIdAndId(tenantId, id);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());

        RelationshipEntity entity = deleteList.get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }
}
