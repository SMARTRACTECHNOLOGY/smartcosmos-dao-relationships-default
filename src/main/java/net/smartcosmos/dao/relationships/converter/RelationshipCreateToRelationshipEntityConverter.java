package net.smartcosmos.dao.relationships.converter;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;


@Component
public class RelationshipCreateToRelationshipEntityConverter
        implements Converter<RelationshipCreate, RelationshipEntity>, FormatterRegistrar {

    @Override
    public RelationshipEntity convert(RelationshipCreate createRelationship) {

        return RelationshipEntity.builder()
            .sourceType(createRelationship.getSource().getType())
            .sourceId(UuidUtil.getUuidFromUrn(createRelationship.getSource().getUrn()))
            .relationshipType(createRelationship.getRelationshipType())
            .targetType(createRelationship.getTarget().getType())
            .targetId(UuidUtil.getUuidFromUrn(createRelationship.getTarget().getUrn()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
