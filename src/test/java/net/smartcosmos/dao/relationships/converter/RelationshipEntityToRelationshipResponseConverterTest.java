package net.smartcosmos.dao.relationships.converter;

import org.junit.*;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.RelationshipResponse;

import static org.junit.Assert.*;

/**
 * RelationshipEntityToRelationshipResponseConverter Tester.
 *
 * @author tcross
 * @version 1.0
 * @since <pre>Jun 28, 2016</pre>
 */
public class RelationshipEntityToRelationshipResponseConverterTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: convert(RelationshipEntity entity)
     */
    @Test
    public void testConvert() throws Exception {
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

        RelationshipEntityToRelationshipResponseConverter converter = new RelationshipEntityToRelationshipResponseConverter();
        RelationshipResponse relationshipResponse = converter.convert(relationshipEntity);

        assertEquals(relationshipResponse.getSource().getType(), relationshipEntity.getSourceType());
        assertEquals(UuidUtil.getUuidFromUrn(relationshipResponse.getSource().getUrn()), relationshipEntity.getSourceId());
        assertEquals(relationshipResponse.getTarget().getType(), relationshipEntity.getTargetType());
        assertEquals(UuidUtil.getUuidFromUrn(relationshipResponse.getTarget().getUrn()), relationshipEntity.getTargetId());
        assertEquals(relationshipResponse.getRelationshipType(), relationshipEntity.getRelationshipType());
    }
}
