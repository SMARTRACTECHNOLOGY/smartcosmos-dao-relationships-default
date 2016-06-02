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

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.accountId
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
    final UUID accountId = UUID.randomUUID();
    private UUID id;
    private UUID referenceId;
    private UUID relatedReferenceId;

    @Before
    public void setUp() throws Exception {
        referenceId = UuidUtil.getNewUuid();
        relatedReferenceId = UuidUtil.getNewUuid();

        RelationshipEntity entity = RelationshipEntity.builder()
                .accountId(accountId)
                .entityReferenceType(TEST_REFERENCE_TYPE)
                .referenceId(referenceId)
                .type(TEST_RELATIONSHIP_TYPE)
                .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
                .relatedReferenceId(relatedReferenceId)
                .moniker(TEST_MONIKER)
                .build();

        entity = relationshipRepository.save(entity);
        id = entity.getId();
    }

    @After
    public void delete() throws Exception {
        relationshipRepository.deleteAll();
    }

    @Test
    public void findByAccountIdAndId() throws Exception {
        assertTrue(this.relationshipRepository.findByAccountIdAndId(accountId, id).isPresent());
    }

    @Test
    public void findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId()
            throws Exception {

        assertTrue(this.relationshipRepository.findByAccountIdAndEntityReferenceTypeAndReferenceIdAndTypeAndRelatedEntityReferenceTypeAndRelatedReferenceId(
                accountId,
                TEST_REFERENCE_TYPE,
                referenceId,
                TEST_RELATIONSHIP_TYPE,
                TEST_REFERENCE_TYPE,
                relatedReferenceId)
            .isPresent());
    }

    @Test
    public void deleteByAccountIdAndId() {
        Optional<RelationshipEntity> optional = relationshipRepository.findByAccountIdAndId(accountId, id);

        assertTrue(optional.isPresent());

        RelationshipEntity entity = optional.get();
        assertEquals(id, entity.getId());
        assertEquals(accountId, entity.getAccountId());
    }
}
