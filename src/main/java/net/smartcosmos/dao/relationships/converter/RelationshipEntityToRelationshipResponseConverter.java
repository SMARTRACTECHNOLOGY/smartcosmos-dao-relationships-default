package net.smartcosmos.dao.relationships.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dao.relationships.util.UuidUtil;
import net.smartcosmos.dto.relationships.RelationshipReference;
import net.smartcosmos.dto.relationships.RelationshipResponse;

@Component
public class RelationshipEntityToRelationshipResponseConverter
    implements Converter<RelationshipEntity, RelationshipResponse>, FormatterRegistrar {

    @Override
    public RelationshipResponse convert(RelationshipEntity entity) {

        RelationshipReference source = RelationshipReference.builder()
            .urn(UuidUtil.getThingUrnFromUuid(entity.getSourceId()))
            .type(entity.getSourceType())
            .build();

        RelationshipReference target = RelationshipReference.builder()
            .urn(UuidUtil.getThingUrnFromUuid(entity.getTargetId()))
            .type(entity.getTargetType())
            .build();

        return RelationshipResponse.builder()
            .urn(UuidUtil.getRelationshipUrnFromUuid(entity.getId()))
            .source(source)
            .target(target)
            .relationshipType(entity.getRelationshipType())
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getTenantId()))
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }
}
