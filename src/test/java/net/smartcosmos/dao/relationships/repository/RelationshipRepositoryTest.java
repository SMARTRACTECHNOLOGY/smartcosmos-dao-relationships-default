package net.smartcosmos.dao.relationships.repository;

import java.util.List;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.smartcosmos.dao.relationships.RelationshipPersistenceConfig;
import net.smartcosmos.dao.relationships.RelationshipPersistenceTestApplication;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.util.UuidUtil;

import static org.junit.Assert.*;

/**
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

        assertTrue(this.relationshipRepository.findByTenantIdAndId(tenantId, id)
                       .isPresent());
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

        Page<RelationshipEntity> entityPage = relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId,
            TEST_RELATIONSHIP_TYPE,
            new PageRequest(0, 1));

        assertEquals(1, entityPage.getTotalPages());

        RelationshipEntity entity = entityPage.getContent()
            .get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityRelatedReferenceTypeAndRelatedReferenceIdAndType() {

        Page<RelationshipEntity> entityPage = relationshipRepository.findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(
            tenantId,
            TEST_REFERENCE_TYPE,
            relatedReferenceId,
            TEST_RELATIONSHIP_TYPE,
            new PageRequest(0, 1));

        assertEquals(1, entityPage.getTotalPages());

        RelationshipEntity entity = entityPage.getContent()
            .get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceIdAndRelatedEntityReferenceTypeAndRelatedEntityReferenceId() {

        Page<RelationshipEntity> entityPage = relationshipRepository.findByTenantIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId,
            TEST_REFERENCE_TYPE,
            relatedReferenceId,
            new PageRequest(0, 1));

        assertEquals(1, entityPage.getTotalPages());

        RelationshipEntity entity = entityPage.getContent()
            .get(0);
        assertEquals(id, entity.getId());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    public void findByTenantIdAndEntityReferenceTypeAndReferenceId() {

        Page<RelationshipEntity> entityPage = relationshipRepository.findByTenantIdAndSourceTypeAndSourceId(
            tenantId,
            TEST_REFERENCE_TYPE,
            referenceId,
            new PageRequest(0, 1));

        assertEquals(1, entityPage.getTotalPages());

        RelationshipEntity entity = entityPage.getContent()
            .get(0);
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
