package net.smartcosmos.dao.relationships.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.smartcosmos.dao.relationships.RelationshipPersistenceConfig;
import net.smartcosmos.dao.relationships.RelationshipPersistenceTestApplication;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipReference;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.security.user.SmartCosmosUser;

import static org.junit.Assert.*;

/**
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.accountId
 */
@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {
    RelationshipPersistenceTestApplication.class,
    RelationshipPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class RelationshipPersistenceServiceTest {

    private final String accountUrn = UuidUtil.getTenantUrnFromUuid(UUID.randomUUID());

    @Autowired
    RelationshipPersistenceService relationshipPersistenceService;

    @Autowired
    RelationshipRepository relationshipRepository;

    @Before
    public void setUp() throws Exception {

        // Need to mock out user for conversion service.
        // Might be a good candidate for a test package util.
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal())
            .thenReturn(new SmartCosmosUser(accountUrn, "urn:userUrn", "username",
                                            "password", Arrays.asList(new SimpleGrantedAuthority("USER"))));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        relationshipRepository.deleteAll();
    }

    @Test
    public void testCreate() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        ;
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);

        assertNotEquals(Optional.empty(), relationshipResponse);

        assertEquals(TEST_SOURCE_TYPE, relationshipResponse.get().getSource().getType()); //.getEntityReferenceType());
        assertEquals(TEST_SOURCE_URN, relationshipResponse.get().getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.get().getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, relationshipResponse.get().getTarget().getType());
        assertEquals(TEST_TARGET_URN, relationshipResponse.get().getTarget().getUrn());
    }

    @Test
    public void testFindByUrn() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found by URN";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate).get().getUrn();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService.findByUrn(accountUrn, urn);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_SOURCE_TYPE, relationshipResponse.get().getSource().getType());
        assertEquals(TEST_SOURCE_URN, relationshipResponse.get().getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.get().getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, relationshipResponse.get().getTarget().getType());
        assertEquals(TEST_TARGET_URN, relationshipResponse.get().getTarget().getUrn());
    }

    @Test
    public void testFindSpecific() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN, TEST_TARGET_TYPE, TEST_TARGET_URN, TEST_RELATIONSHIP_TYPE);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_SOURCE_TYPE, relationshipResponse.get().getSource().getType());
        assertEquals(TEST_SOURCE_URN, relationshipResponse.get().getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.get().getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, relationshipResponse.get().getTarget().getType());
        assertEquals(TEST_TARGET_URN, relationshipResponse.get().getTarget().getUrn());
    }

    @Test
    public void testDelete() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Delete by URN";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate).get().getUrn();

        assertTrue(relationshipPersistenceService.findByUrn(accountUrn, urn).isPresent());

        relationshipPersistenceService.delete(accountUrn, urn);

        assertFalse(relationshipPersistenceService.findByUrn(accountUrn, urn).isPresent());
    }

    @Test
    public void testFindBySourceType() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForSource(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);

        assertEquals(1, responsePage.getPage().getSize());
        RelationshipResponse response = responsePage.getData().get(0);
        assertEquals(TEST_SOURCE_TYPE, response.getSource().getType());
        assertEquals(TEST_SOURCE_URN, response.getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, response.getTarget().getType());
        assertEquals(TEST_TARGET_URN, response.getTarget().getUrn());
    }

    @Test
    public void testFindByTargetType() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForTarget(accountUrn, TEST_TARGET_TYPE, TEST_TARGET_URN,
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);

        assertEquals(1, responsePage.getPage().getSize());
        RelationshipResponse response = responsePage.getData().get(0);
        assertEquals(TEST_SOURCE_TYPE, response.getSource().getType());
        assertEquals(TEST_SOURCE_URN, response.getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, response.getTarget().getType());
        assertEquals(TEST_TARGET_URN, response.getTarget().getUrn());
    }

    /*
    @Ignore
    @Test
    public void testFindAll() {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAll(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN);

        assertEquals(1, responsePage.getPage().getSize());
        RelationshipResponse response = responsePage.getData().get(0);
        assertEquals(TEST_SOURCE_TYPE, response.getSource().getType());
        assertEquals(TEST_SOURCE_URN, response.getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, response.getTarget().getType());
        assertEquals(TEST_TARGET_URN, response.getTarget().getUrn());
    }
    */

    /*
    @Ignore
    @Test
    public void testFindBetweenEntities() {
        final String TEST_SOURCE_URN = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build())
            .target(RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();


        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findBetweenEntities(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                  TEST_TARGET_TYPE, TEST_TARGET_URN, null, null,
                                                                                                     null, null);

        assertEquals(1, responsePage.getPage().getSize());
        RelationshipResponse response = responsePage.getData().get(0);
        assertEquals(TEST_SOURCE_TYPE, response.getSource().getType());
        assertEquals(TEST_SOURCE_URN, response.getSource().getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE, response.getTarget().getType());
        assertEquals(TEST_TARGET_URN, response.getTarget().getUrn());
    }
    */
}
