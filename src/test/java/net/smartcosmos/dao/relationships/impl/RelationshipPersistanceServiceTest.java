package net.smartcosmos.dao.relationships.impl;

import net.smartcosmos.dao.relationships.RelationshipPersistenceConfig;
import net.smartcosmos.dao.relationships.RelationshipPersistenceTestApplication;
import net.smartcosmos.dao.relationships.repository.RelationshipRepository;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipLookupSpecific;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.util.UuidUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 *
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
public class RelationshipPersistanceServiceTest {

    private final UUID accountId = UUID.randomUUID();
    private final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountId);

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
        final String TEST_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_RELATED_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_REFERENCE_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .entityReferenceType(TEST_REFERENCE_TYPE)
            .referenceUrn(TEST_ENTITY)
            .type(TEST_RELATIONSHIP_TYPE)
            .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
            .relatedReferenceUrn(TEST_RELATED_ENTITY)
            .moniker(TEST_MONIKER)
            .build();

        RelationshipResponse relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);

        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.getEntityReferenceType());
        assertEquals(TEST_ENTITY, relationshipResponse.getReferenceUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.getType());
        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.getRelatedEntityReferenceType());
        assertEquals(TEST_RELATED_ENTITY, relationshipResponse.getRelatedReferenceUrn());
        assertEquals(TEST_MONIKER, relationshipResponse.getMoniker());
    }

    @Test
    public void testFindByUrn() {
        final String TEST_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_RELATED_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_REFERENCE_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found by URN";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .entityReferenceType(TEST_REFERENCE_TYPE)
            .referenceUrn(TEST_ENTITY)
            .type(TEST_RELATIONSHIP_TYPE)
            .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
            .relatedReferenceUrn(TEST_RELATED_ENTITY)
            .moniker(TEST_MONIKER)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate).getUrn();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService.findByUrn(accountUrn, urn);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.get().getEntityReferenceType());
        assertEquals(TEST_ENTITY, relationshipResponse.get().getReferenceUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.get().getType());
        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.get().getRelatedEntityReferenceType());
        assertEquals(TEST_RELATED_ENTITY, relationshipResponse.get().getRelatedReferenceUrn());
        assertEquals(TEST_MONIKER, relationshipResponse.get().getMoniker());
    }

    @Test
    public void testFindSpecific() {
        final String TEST_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_RELATED_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_REFERENCE_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .entityReferenceType(TEST_REFERENCE_TYPE)
            .referenceUrn(TEST_ENTITY)
            .type(TEST_RELATIONSHIP_TYPE)
            .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
            .relatedReferenceUrn(TEST_RELATED_ENTITY)
            .moniker(TEST_MONIKER)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        RelationshipLookupSpecific relationshipLookupSpecific = RelationshipLookupSpecific.builder()
            .entityReferenceType(TEST_REFERENCE_TYPE)
            .referenceUrn(TEST_ENTITY)
            .type(TEST_RELATIONSHIP_TYPE)
            .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
            .relatedReferenceUrn(TEST_RELATED_ENTITY)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, relationshipLookupSpecific);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.get().getEntityReferenceType());
        assertEquals(TEST_ENTITY, relationshipResponse.get().getReferenceUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, relationshipResponse.get().getType());
        assertEquals(TEST_REFERENCE_TYPE, relationshipResponse.get().getRelatedEntityReferenceType());
        assertEquals(TEST_RELATED_ENTITY, relationshipResponse.get().getRelatedReferenceUrn());
        assertEquals(TEST_MONIKER, relationshipResponse.get().getMoniker());
    }

    @Test
    public void testDelete() {
        final String TEST_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_RELATED_ENTITY = "urn:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_REFERENCE_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Delete by URN";
        final String TEST_MONIKER = "Moniker";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .entityReferenceType(TEST_REFERENCE_TYPE)
            .referenceUrn(TEST_ENTITY)
            .type(TEST_RELATIONSHIP_TYPE)
            .relatedEntityReferenceType(TEST_REFERENCE_TYPE)
            .relatedReferenceUrn(TEST_RELATED_ENTITY)
            .moniker(TEST_MONIKER)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate).getUrn();

        assertTrue(relationshipPersistenceService.findByUrn(accountUrn, urn).isPresent());

        relationshipPersistenceService.delete(accountUrn, urn);

        assertFalse(relationshipPersistenceService.findByUrn(accountUrn, urn).isPresent());
    }

    @Test
    public void testUpdateMoniker() {

    }

}
