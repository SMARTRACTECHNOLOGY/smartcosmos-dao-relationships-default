package net.smartcosmos.dao.relationships.converter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.PageInformation;
import net.smartcosmos.dto.relationships.RelationshipResponse;

@Component
public class SpringDataPageToRelationshipResponsePageConverter
    extends ConversionServiceAwareConverter<org.springframework.data.domain.Page<RelationshipEntity>, Page<RelationshipResponse>> {

    @Autowired
    private ConversionService conversionService;

    protected ConversionService conversionService() {

        return conversionService;
    }

    @Override
    public Page<RelationshipResponse> convert(org.springframework.data.domain.Page<RelationshipEntity> page) {

        PageInformation pageInformation = PageInformation.builder()
            .number(page.getNumber() + 1)
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages(page.getTotalPages())
            .build();

        List<RelationshipResponse> data = page.getContent()
            .stream()
            .map(entity -> conversionService.convert(entity, RelationshipResponse.class))
            .collect(Collectors.toList());

        return Page.<RelationshipResponse>builder()
            .data(data)
            .page(pageInformation)
            .build();
    }
}
