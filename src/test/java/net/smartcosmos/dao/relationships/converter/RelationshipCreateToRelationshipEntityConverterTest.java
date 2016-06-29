package net.smartcosmos.dao.relationships.converter;

import org.junit.*;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.dto.relationships.RelationshipReference;

import static org.junit.Assert.*;

/**
 * RelationshipCreateToRelationshipEntityConverter Tester.
 *
 * @author tcross
 * @version 1.0
 * @since <pre>Jun 28, 2016</pre>
 */
public class RelationshipCreateToRelationshipEntityConverterTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: convert(RelationshipCreate createRelationship)
     */
    @Test
    public void testConvert() throws Exception {
        final String TEST_SOURCE_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_TARGET_URN = "urn:thing:uuid:" + UuidUtil.getNewUuidAsString();
        final String TEST_SOURCE_TYPE = "Thing";
        final String TEST_TARGET_TYPE = "Thing";
        final String TEST_RELATIONSHIP_TYPE = "Created by";

        RelationshipReference source = RelationshipReference.builder().type(TEST_SOURCE_TYPE).urn(TEST_SOURCE_URN).build();
        RelationshipReference target = RelationshipReference.builder().type(TEST_TARGET_TYPE).urn(TEST_TARGET_URN).build();
        RelationshipCreate relationshipCreate = RelationshipCreate.builder().source(source).target(target).relationshipType(TEST_RELATIONSHIP_TYPE)
            .build();

        RelationshipCreateToRelationshipEntityConverter converter = new RelationshipCreateToRelationshipEntityConverter();
        RelationshipEntity relationshipEntity = converter.convert(relationshipCreate);

        assertEquals(relationshipCreate.getSource().getType(), relationshipEntity.getSourceType());
        assertEquals(UuidUtil.getUuidFromUrn(relationshipCreate.getSource().getUrn()), relationshipEntity.getSourceId());
        assertEquals(relationshipCreate.getTarget().getType(), relationshipEntity.getTargetType());
        assertEquals(UuidUtil.getUuidFromUrn(relationshipCreate.getTarget().getUrn()), relationshipEntity.getTargetId());
        assertEquals(relationshipCreate.getRelationshipType(), relationshipEntity.getRelationshipType());
    }
}
