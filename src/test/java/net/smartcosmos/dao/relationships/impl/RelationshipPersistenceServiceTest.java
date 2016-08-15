package net.smartcosmos.dao.relationships.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.ConstraintViolationException;

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
        Mockito.when(securityContext.getAuthentication())
            .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {

        relationshipRepository.deleteAll();
    }

    @Test
    public void testCreateSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);

        assertNotEquals(Optional.empty(), relationshipResponse);

        assertEquals(TEST_SOURCE_TYPE,
                     relationshipResponse.get()
                         .getSource()
                         .getType()); //.getEntityReferenceType());
        assertEquals(TEST_SOURCE_URN,
                     relationshipResponse.get()
                         .getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE,
                     relationshipResponse.get()
                         .getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     relationshipResponse.get()
                         .getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     relationshipResponse.get()
                         .getTarget()
                         .getUrn());
    }

    @Test
    public void testCreateReturnsEmptyOptionalOnSecondCreateOfSameRelationship() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);

        assertNotEquals(Optional.empty(), relationshipResponse);

        assertEquals(TEST_SOURCE_TYPE,
                     relationshipResponse.get()
                         .getSource()
                         .getType()); //.getEntityReferenceType());
        assertEquals(TEST_SOURCE_URN,
                     relationshipResponse.get()
                         .getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE,
                     relationshipResponse.get()
                         .getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     relationshipResponse.get()
                         .getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     relationshipResponse.get()
                         .getTarget()
                         .getUrn());

        Optional<RelationshipResponse> relationshipResponse2 = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);

        assertEquals(Optional.empty(), relationshipResponse2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateThrowsIllegalArgumentExceptionOnBadSourceUrn() {

        final String TEST_SOURCE_URN = "nonConformingUrn:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateThrowsIllegalArgumentExceptionOnBadTargetUrn() {

        final String TEST_SOURCE_URN = "nonConformingUrn:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = null;
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateThrowsConstraintViolationExceptionOnBadSourceType() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = null;
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateThrowsConstraintViolationExceptionOnBadTargetType() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = null;
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateThrowsConstraintViolationExceptionOnBadRelationshipType() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = null;

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .create(accountUrn, relationshipCreate);
    }

    @Test
    public void testFindByUrnSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found by URN";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate)
            .get()
            .getUrn();

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService.findByUrn(accountUrn, urn);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_SOURCE_TYPE,
                     relationshipResponse.get()
                         .getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     relationshipResponse.get()
                         .getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE,
                     relationshipResponse.get()
                         .getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     relationshipResponse.get()
                         .getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     relationshipResponse.get()
                         .getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByUrnMalformedUrnThrowsIllegalArgumentExceptionOnBadSourceUrn() {

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService.findByUrn(accountUrn, "malformedUrn");

    }

    @Test
    public void testFindByUrnNonexistentUrnReturnsEmptyOptionalOnNotFound() {

        final String SOME_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService.findByUrn(accountUrn, SOME_URN);
        assertFalse(relationshipResponse.isPresent());

    }

    @Test
    public void testFindSpecificSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN, TEST_TARGET_TYPE, TEST_TARGET_URN, TEST_RELATIONSHIP_TYPE);

        assertTrue(relationshipResponse.isPresent());
        assertEquals(TEST_SOURCE_TYPE,
                     relationshipResponse.get()
                         .getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     relationshipResponse.get()
                         .getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE,
                     relationshipResponse.get()
                         .getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     relationshipResponse.get()
                         .getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     relationshipResponse.get()
                         .getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindSpecificThrowsIllegalArgumentExceptionOnBadSourceUrn() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, TEST_SOURCE_TYPE, "nonConformingUrn", TEST_TARGET_TYPE, TEST_TARGET_URN, TEST_RELATIONSHIP_TYPE);

    }

    @Test
    public void testFindSpecificReturnsEmptyOptionalOnBadSourceType() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, null, TEST_SOURCE_URN, TEST_TARGET_TYPE, TEST_TARGET_URN, TEST_RELATIONSHIP_TYPE);
        assertFalse(relationshipResponse.isPresent());
    }

    @Test
    public void testFindSpecificReturnsEmptyOptionalOnBadRelationshipType() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Found specific";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Optional<RelationshipResponse> relationshipResponse = relationshipPersistenceService
            .findSpecific(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN, TEST_TARGET_TYPE, TEST_TARGET_URN, "someRelationshipType");
        assertFalse(relationshipResponse.isPresent());
    }

    @Test
    public void testDeleteSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Delete by URN";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate)
            .get()
            .getUrn();
        assertTrue(relationshipPersistenceService.findByUrn(accountUrn, urn)
                       .isPresent());
        List<RelationshipResponse> returnList = relationshipPersistenceService.delete(accountUrn, urn);

        assertEquals(returnList.size(), 1);
        assertFalse(relationshipPersistenceService.findByUrn(accountUrn, urn)
                        .isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteThrowsIllegalArgumentExceptionOnMalformedUrn() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Delete by URN";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        String urn = relationshipPersistenceService.create(accountUrn, relationshipCreate)
            .get()
            .getUrn();
        assertTrue(relationshipPersistenceService.findByUrn(accountUrn, urn)
                       .isPresent());
        List<RelationshipResponse> returnList = relationshipPersistenceService.delete(accountUrn, "malformedUrn");
    }

    @Test
    public void testDeleteReturnsEmptyResponseListOnNonexistent() {

        final String SOME_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        List<RelationshipResponse> returnList = relationshipPersistenceService.delete(accountUrn, SOME_URN);
        assertEquals(returnList.size(), 0);
    }

    @Test
    public void testFindBySourceTypeSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForSource(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);

        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        RelationshipResponse response = responsePage.getData()
            .get(0);
        assertEquals(TEST_SOURCE_TYPE,
                     response.getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     response.getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     response.getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     response.getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindBySourceTypeThrowsIllegalArguementExceptionOnMalformedUrn() {

        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "RelationshipType";

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForSource(accountUrn, TEST_SOURCE_TYPE, "malformedUrn",
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);
    }

    @Test
    public void testFindBySourceTypeReturnsEmptyPageOnNotFound() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForSource(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                     "someOtherType", null, null, null, null);
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindByTargetTypeSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForTarget(accountUrn, TEST_TARGET_TYPE, TEST_TARGET_URN,
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);

        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        RelationshipResponse response = responsePage.getData()
            .get(0);
        assertEquals(TEST_SOURCE_TYPE,
                     response.getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     response.getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     response.getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     response.getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByTargetTypeThrowsIllegalArguementExceptionOnMalformedUrn() {

        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "RelationshipType";

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForTarget(accountUrn, TEST_TARGET_TYPE, "malformedUrn",
                                                                                                     TEST_RELATIONSHIP_TYPE, null, null, null, null);
    }

    @Test
    public void testFindByTargetTypeReturnsEmptyPageOnNotFound() {

        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_TYPE = "Thing";

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findByTypeForTarget(accountUrn, TEST_TARGET_TYPE, TEST_TARGET_URN,
                                                                                                     "someOtherType", null, null, null, null);
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindAllForSourceSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForSource(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                  null, null, null, null);

        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        RelationshipResponse response = responsePage.getData()
            .get(0);
        assertEquals(TEST_SOURCE_TYPE,
                     response.getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     response.getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     response.getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     response.getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAllForSourceThrowsIllegalArgumentExceptionOnMalformedUrn() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForSource(accountUrn, TEST_SOURCE_TYPE, "malformedUrn",
                                                                                                  null, null, null, null);

    }

    @Test
    public void testFindAllForSourceReturnsEmptyPageIfNotFound() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForSource(accountUrn, "someType", TEST_SOURCE_URN,
                                                                                                  null, null, null, null);

        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindAllForTargetSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForTarget(accountUrn, TEST_TARGET_TYPE, TEST_TARGET_URN,
                                                                                                  null, null, null, null);

        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        RelationshipResponse response = responsePage.getData()
            .get(0);
        assertEquals(TEST_SOURCE_TYPE,
                     response.getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     response.getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     response.getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     response.getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAllForTargetThrowsIllegalArgumentExceptionOnMalformedUrn() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForTarget(accountUrn, TEST_TARGET_TYPE, "malformedUrn",
                                                                                                  null, null, null, null);

    }

    @Test
    public void testFindAllForTargetReturnsEmptyPageIfNotFound() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findAllForTarget(accountUrn, "someType", TEST_TARGET_URN,
                                                                                                  null, null, null, null);

        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

    @Test
    public void testFindBetweenEntitiesSuccess() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findBetweenEntities(accountUrn, TEST_SOURCE_TYPE, TEST_SOURCE_URN,
                                                                                                     TEST_TARGET_TYPE, TEST_TARGET_URN, null, null,
                                                                                                     null, null);

        assertEquals(1,
                     responsePage.getPage()
                         .getSize());
        RelationshipResponse response = responsePage.getData()
            .get(0);
        assertEquals(TEST_SOURCE_TYPE,
                     response.getSource()
                         .getType());
        assertEquals(TEST_SOURCE_URN,
                     response.getSource()
                         .getUrn());
        assertEquals(TEST_RELATIONSHIP_TYPE, response.getRelationshipType());
        assertEquals(TEST_TARGET_TYPE,
                     response.getTarget()
                         .getType());
        assertEquals(TEST_TARGET_URN,
                     response.getTarget()
                         .getUrn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindBetweenEntitiesThrowsIllegalArgumentExceptionOnMalformedUrn() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);

        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findBetweenEntities(accountUrn, TEST_SOURCE_TYPE, "malformedUrn",
                                                                                                     TEST_TARGET_TYPE, TEST_TARGET_URN, null, null,
                                                                                                     null, null);
    }

    @Test
    public void testFindBetweenEntitiesReturnsEmptyPageOnNotFound() {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Type";

        RelationshipCreate relationshipCreate = RelationshipCreate.builder()
            .source(RelationshipReference.builder()
                        .type(TEST_SOURCE_TYPE)
                        .urn(TEST_SOURCE_URN)
                        .build())
            .target(RelationshipReference.builder()
                        .type(TEST_TARGET_TYPE)
                        .urn(TEST_TARGET_URN)
                        .build())
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        relationshipPersistenceService.create(accountUrn, relationshipCreate);
        Page<RelationshipResponse> responsePage = relationshipPersistenceService.findBetweenEntities(accountUrn, "someType", TEST_SOURCE_URN,
                                                                                                     TEST_TARGET_TYPE, TEST_TARGET_URN, null, null,
                                                                                                     null, null);
        assertEquals(0,
                     responsePage.getPage()
                         .getSize());
        assertEquals(0,
                     responsePage.getPage()
                         .getNumber());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalElements());
        assertEquals(0,
                     responsePage.getPage()
                         .getTotalPages());
    }

}
