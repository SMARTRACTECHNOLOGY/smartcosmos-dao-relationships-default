package net.smartcosmos.dao.relationships.converter;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dto.relationships.RelationshipCreate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;

import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;


@Component
public class RelationshipCreateToRelationshipEntityConverter
        implements Converter<RelationshipCreate, RelationshipEntity>, FormatterRegistrar {

    @Override
    public RelationshipEntity convert(RelationshipCreate relationshipCreate) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return RelationshipEntity.builder()
                // Required
                .entityReferenceType(relationshipCreate.getEntityReferenceType())
                .referenceId(UuidUtil.getUuidFromUrn(relationshipCreate.getReferenceUrn()))
                .type(relationshipCreate.getType())
                .relatedEntityReferenceType(relationshipCreate.getRelatedEntityReferenceType())
                .relatedReferenceId(UuidUtil.getUuidFromUrn(relationshipCreate.getRelatedReferenceUrn()))
                .accountId(UuidUtil.getUuidFromAccountUrn(user.getAccountUrn()))
                // Optional
                .moniker(relationshipCreate.getMoniker()).build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
