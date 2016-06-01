package net.smartcosmos.dao.relationships.domain;

import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class RelationshipEntityTest {

    private static Validator validator;

    private static final UUID ID = UuidUtil.getNewUuid();
    private static final String TYPE = RandomStringUtils.randomAlphanumeric(255);
    private static final String TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String MONIKER = RandomStringUtils.randomAlphanumeric(2048);
    private static final String MONIKER_INVALID = RandomStringUtils.randomAlphanumeric(2049);
    private static final UUID ACCOUNT_ID = UuidUtil.getNewUuid();

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .entityReferenceType(TYPE)
            .referenceId(ID)
            .type(TYPE)
            .relatedEntityReferenceType(TYPE)
            .relatedReferenceId(ID)
            .moniker(MONIKER)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatNullMonikerIsOk() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .entityReferenceType(TYPE)
            .referenceId(ID)
            .type(TYPE)
            .relatedEntityReferenceType(TYPE)
            .relatedReferenceId(ID)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertTrue(violationSet.isEmpty());
    }
}
