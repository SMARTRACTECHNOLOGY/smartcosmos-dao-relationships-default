package net.smartcosmos.dao.relationships.converter;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dto.relationships.RelationshipUpsert;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;


@Component
public class RelationshipCreateToRelationshipEntityConverter
        implements Converter<RelationshipUpsert, RelationshipEntity>, FormatterRegistrar {

    @Override
    public RelationshipEntity convert(RelationshipUpsert relationshipUpsert) {

        return RelationshipEntity.builder()
                // Required
                .entityReferenceType(relationshipUpsert.getEntityReferenceType())
                .referenceId(UuidUtil.getUuidFromUrn(relationshipUpsert.getReferenceUrn()))
                .type(relationshipUpsert.getType())
                .relatedEntityReferenceType(relationshipUpsert.getRelatedEntityReferenceType())
                .relatedReferenceId(UuidUtil.getUuidFromUrn(relationshipUpsert.getRelatedReferenceUrn()))
                // Optional
                .moniker(relationshipUpsert.getMoniker()).build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
