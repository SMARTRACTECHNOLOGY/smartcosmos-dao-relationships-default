package net.smartcosmos.dao.relationships.converter;

import org.junit.*;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class AbstractConverterTest {

    protected GenericConversionService conversionService;

    @Before
    public void setup() {

        RelationshipEntityToRelationshipResponseConverter responseConverter =
            new RelationshipEntityToRelationshipResponseConverter();

        SpringDataPageToRelationshipResponsePageConverter pagedResponseConverter =
            new SpringDataPageToRelationshipResponsePageConverter();

        conversionService = new GenericConversionService();
        conversionService.addConverter(responseConverter);
        conversionService.addConverter(pagedResponseConverter);

        ReflectionTestUtils.setField(
            pagedResponseConverter,
            "conversionService",
            conversionService);
    }
}
