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
            .sourceType(TYPE)
            .sourceId(ID)
            .relationshipType(TYPE)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatEmptySourceTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceId(ID)
            .sourceType("")
            .relationshipType(TYPE)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("sourceType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatNullSourceIdIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .relationshipType(TYPE)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("sourceId", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatEmptyRelationshipTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .sourceId(ID)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("relationshipType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatEmptyTargetTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .sourceId(ID)
            .relationshipType(TYPE)
            .targetId(ID)
            .targetType("")
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("targetType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatNullTargetIdIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .sourceId(ID)
            .relationshipType(TYPE)
            .targetType(TYPE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("targetId", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatInvalidSourceTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE_INVALID)
            .sourceId(ID)
            .relationshipType(TYPE)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();
        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("sourceType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatInvalidRelationshipTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .sourceId(ID)
            .relationshipType(TYPE_INVALID)
            .targetType(TYPE)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();
        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("relationshipType", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatInvalidTargetTypeIsFailure() {

        RelationshipEntity relationshipEntity = RelationshipEntity.builder()
            .id(ID)
            .sourceType(TYPE)
            .sourceId(ID)
            .relationshipType(TYPE)
            .targetType(TYPE_INVALID)
            .targetId(ID)
            .tenantId(ACCOUNT_ID)
            .build();
        Set<ConstraintViolation<RelationshipEntity>> violationSet = validator.validate(relationshipEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("targetType", violationSet.iterator().next().getPropertyPath().toString());
    }
}
