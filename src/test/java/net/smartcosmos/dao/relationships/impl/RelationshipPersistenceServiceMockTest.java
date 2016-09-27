package net.smartcosmos.dao.relationships.impl;

import java.util.UUID;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;

import net.smartcosmos.dao.relationships.repository.RelationshipRepository;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RelationshipPersistenceServiceMockTest {

    @Mock
    RelationshipRepository relationshipRepository;

    @Mock
    ConversionService conversionService;

    @InjectMocks
    RelationshipPersistenceService service;

    // region Setup

    @After
    public void tearDown() {

        reset(relationshipRepository, conversionService);
    }

    @Test
    public void thatMockingWorks() {

        assertNotNull(relationshipRepository);
        assertNotNull(conversionService);
        assertNotNull(service);
    }

    // endregion

    // region findByTypeForSource()

    @Test
    public void thatFindByTypeForSourceCallsRepositoryMethodWithTenant() {

        final String tenantUrn = "urn:tenant:uuid:1c5dd08d-908c-4375-b62b-720c2aacc11d";
        final String sourceType = "someSourceType";
        final String sourceUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForSource(tenantUrn, sourceType, sourceUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findByTenantIdAndSourceTypeAndSourceIdAndRelationshipType(any(UUID.class),
                                                                                                           eq(sourceType),
                                                                                                           any(UUID.class),
                                                                                                           eq(relationshipType),
                                                                                                           any(Pageable.class));
    }

    @Test
    public void thatFindByTypeForSourceCallsRepositoryMethodWithoutTenantInCaseOfEmptyTenantUrn() {

        final String tenantUrn = "";
        final String sourceType = "someSourceType";
        final String sourceUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForSource(tenantUrn, sourceType, sourceUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findBySourceTypeAndSourceIdAndRelationshipType(eq(sourceType),
                                                                                                any(UUID.class),
                                                                                                eq(relationshipType),
                                                                                                any(Pageable.class));
    }

    @Test
    public void thatFindByTypeForSourceCallsRepositoryMethodWithoutTenantInCaseOfNullTenantUrn() {

        final String tenantUrn = null;
        final String sourceType = "someSourceType";
        final String sourceUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForSource(tenantUrn, sourceType, sourceUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findBySourceTypeAndSourceIdAndRelationshipType(eq(sourceType),
                                                                                                any(UUID.class),
                                                                                                eq(relationshipType),
                                                                                                any(Pageable.class));
    }

    // endregion

    // region findByTypeForTarget()

    @Test
    public void thatFindByTypeForTargetCallsRepositoryMethodWithTenant() {

        final String tenantUrn = "urn:tenant:uuid:1c5dd08d-908c-4375-b62b-720c2aacc11d";
        final String targetType = "someTargetType";
        final String targetUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForTarget(tenantUrn, targetType, targetUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findByTenantIdAndTargetTypeAndTargetIdAndRelationshipType(any(UUID.class),
                                                                                                           eq(targetType),
                                                                                                           any(UUID.class),
                                                                                                           eq(relationshipType),
                                                                                                           any(Pageable.class));
    }

    @Test
    public void thatFindByTypeForTargetCallsRepositoryMethodWithoutTenantInCaseOfEmptyTenantUrn() {

        final String tenantUrn = "";
        final String targetType = "someTargetType";
        final String targetUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForTarget(tenantUrn, targetType, targetUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findByTargetTypeAndTargetIdAndRelationshipType(eq(targetType),
                                                                                                any(UUID.class),
                                                                                                eq(relationshipType),
                                                                                                any(Pageable.class));
    }

    @Test
    public void thatFindByTypeForTargetCallsRepositoryMethodWithoutTenantInCaseOfNullTenantUrn() {

        final String tenantUrn = null;
        final String targetType = "someTargetType";
        final String targetUrn = "urn:thing:uuid:8658ae3f-7e7c-4f08-b859-ab179f09f573";
        final String relationshipType = "someRelationshipType";
        final Integer page = 1;
        final Integer size = 20;

        service.findByTypeForTarget(tenantUrn, targetType, targetUrn, relationshipType, page, size, null, null);

        verify(relationshipRepository, times(1)).findByTargetTypeAndTargetIdAndRelationshipType(eq(targetType),
                                                                                                any(UUID.class),
                                                                                                eq(relationshipType),
                                                                                                any(Pageable.class));
    }

    // endregion
}
