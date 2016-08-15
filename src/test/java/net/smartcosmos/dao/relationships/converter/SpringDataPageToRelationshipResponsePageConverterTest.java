package net.smartcosmos.dao.relationships.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.springframework.data.domain.PageImpl;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.util.RelationshipPersistenceUtil;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.PageInformation;
import net.smartcosmos.dto.relationships.RelationshipResponse;

import static org.junit.Assert.*;

/**
 * SpringDataPageToRelationshipResponsePageConverter Tester.
 *
 * @author tcross
 * @version 1.0
 * @since <pre>Jun 28, 2016</pre>
 */
public class SpringDataPageToRelationshipResponsePageConverterTest extends AbstractConverterTest {

    @Test
    public void testConversionService() throws Exception {

        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipEntity relationshipEntity = RelationshipEntity
            .builder()
            .id(UuidUtil.getNewUuid())
            .tenantId(UuidUtil.getNewUuid())
            .sourceId(UuidUtil.getUuidFromUrn(TEST_SOURCE_URN))
            .sourceType(TEST_SOURCE_TYPE)
            .targetId(UuidUtil.getUuidFromUrn(TEST_TARGET_URN))
            .targetType(TEST_TARGET_TYPE)
            .relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();
        List<RelationshipEntity> content = new ArrayList<>();
        content.add(relationshipEntity);

        org.springframework.data.domain.Page<RelationshipEntity> entityPage = new PageImpl<>(content);
        Page<RelationshipResponse> convertedPage = conversionService.convert(entityPage, Page.class);
        RelationshipResponse response = convertedPage.getData()
            .get(0);

        assertEquals(convertedPage.getData()
                         .size(), 1);
        assertEquals(response.getSource()
                         .getType(), relationshipEntity.getSourceType());
        assertEquals(response.getSource()
                         .getUrn(), UuidUtil.getThingUrnFromUuid(relationshipEntity.getSourceId()));
        assertEquals(response.getTarget()
                         .getType(), relationshipEntity.getTargetType());
        assertEquals(response.getTarget()
                         .getUrn(), UuidUtil.getThingUrnFromUuid(relationshipEntity.getTargetId()));
        assertEquals(response.getRelationshipType(), relationshipEntity.getRelationshipType());
    }

    @Test
    public void thatEmptyPageConversionSucceeds() {

        List<RelationshipEntity> content = new ArrayList<>();
        org.springframework.data.domain.Page<RelationshipEntity> emptyEntityPage = new PageImpl<>(content);

        Page<RelationshipResponse> convertedPage = conversionService.convert(emptyEntityPage, Page.class);

        Collection<RelationshipResponse> data = convertedPage.getData();

        assertTrue(data.isEmpty());

        PageInformation pageInformation = convertedPage.getPage();

        assertEquals(0, pageInformation.getSize());
        assertEquals(0, pageInformation.getNumber());
        assertEquals(0, pageInformation.getTotalPages());
        assertEquals(0, pageInformation.getTotalElements());

        Page<RelationshipResponse> emptyPage = RelationshipPersistenceUtil.emptyPage();
        assertEquals(emptyPage, convertedPage);
    }

}
