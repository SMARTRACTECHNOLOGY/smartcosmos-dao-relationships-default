package net.smartcosmos.dao.relationships.converter;;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dto.relationships.RelationshipResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class RelationshipEntityToRelationshipResponseConverter
        implements Converter<RelationshipEntity, RelationshipResponse>, FormatterRegistrar {

    @Override
    public RelationshipResponse convert(RelationshipEntity entity) {

        if (entity == null) {
            return null;
        }

        return RelationshipResponse.builder()
                // Required
                .entityReferenceType(entity.getEntityReferenceType())
                .referenceUrn(entity.getReferenceUrn())
                .type(entity.getType())
                .relatedEntityReferenceType(entity.getRelatedEntityReferenceType())
                .relatedReferenceUrn(entity.getRelatedReferenceUrn())
                .urn(UuidUtil.getUrnFromUuid(entity.getId()))
                .accountUrn(UuidUtil.getAccountUrnFromUuid(entity.getAccountId()))
                // Optional
                .moniker(entity.getMoniker())
                // Don't forget to build it!
                .build();
    }

    public List convertAll(Iterable<RelationshipEntity> entities) {
        List<RelationshipResponse> convertedList = new ArrayList<>();
        for (RelationshipEntity entity: entities) {
            convertedList.add(convert(entity));
        }
        return convertedList;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
