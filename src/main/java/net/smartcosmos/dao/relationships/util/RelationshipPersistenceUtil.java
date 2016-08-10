package net.smartcosmos.dao.relationships.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import net.smartcosmos.dao.relationships.SortOrder;
import net.smartcosmos.dao.relationships.domain.RelationshipEntity;
import net.smartcosmos.dto.relationships.Page;
import net.smartcosmos.dto.relationships.RelationshipResponse;

public class RelationshipPersistenceUtil {

    /**
     * Transforms a field name for a sorted query to a valid case-sensitive field name that exists in the entity class.
     * Returns the input field name, if it does not exist in the entity class.
     *
     * @param fieldName the input field name
     * @return the case-corrected field name
     */
    public static String normalizeFieldName(String fieldName) {

        if (StringUtils.equalsIgnoreCase("urn", fieldName) || StringUtils.equalsIgnoreCase("id", fieldName)) {
            return "id";
        }

        if (StringUtils.equalsIgnoreCase("sourceType", fieldName)) {
            return "sourceType";
        }

        if (StringUtils.equalsIgnoreCase("sourceUrn", fieldName)) {
            return "sourceUrn";
        }

        if (StringUtils.equalsIgnoreCase("relationshipType", fieldName)) {
            return "relationshipType";
        }

        if (StringUtils.equalsIgnoreCase("targetType", fieldName)) {
            return "targetType";
        }

        if (StringUtils.equalsIgnoreCase("targetUrn", fieldName)) {
            return "targetUrn";
        }

        if (StringUtils.equalsIgnoreCase("tenantUrn", fieldName) || StringUtils.equalsIgnoreCase("tenantId", fieldName)) {
            return "tenantId";
        }

        if (StringUtils.equalsIgnoreCase("created", fieldName)) {
            return "created";
        }

        if (StringUtils.equalsIgnoreCase("lastModified", fieldName)) {
            return "lastModified";
        }

        return fieldName;
    }

    /**
     * Checks if a given field name exists in {@link RelationshipEntity}.
     *
     * @param fieldName the field name
     * @return {@code true} if the field exists
     */
    public static boolean isRelationshipEntityField(String fieldName) {

        try {
            RelationshipEntity.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets a valid field name for a {@code sortBy} query in the {@link RelationshipEntity} data base.
     * The input field name is case-corrected and replaced by {@code id} if it does not exist in the entity class.
     *
     * @param sortBy the input field name
     * @return the case-corrected field name if it exists, {@code id} otherwise
     */
    public static String getSortByFieldName(String sortBy) {

        sortBy = RelationshipPersistenceUtil.normalizeFieldName(sortBy);
        if (StringUtils.isBlank(sortBy) || !RelationshipPersistenceUtil.isRelationshipEntityField(sortBy)) {
            sortBy = "id";
        }
        return sortBy;
    }

    /**
     * Converts the {@link SortOrder} value to a Spring-compatible {@link Sort.Direction} sort direction.
     *
     * @param sortOrder the sort order
     * @return the Spring sort direction
     */
    public static Sort.Direction getSortDirection(SortOrder sortOrder) {

        Sort.Direction direction = Sort.DEFAULT_DIRECTION;
        switch (sortOrder) {
            case ASC:
                direction = Sort.Direction.ASC;
                break;
            case DESC:
                direction = Sort.Direction.DESC;
                break;
        }
        return direction;
    }

    /**
     * Creates an empty {@link Page<RelationshipResponse>} instance.
     *
     * @return the empty page
     */
    public static Page<RelationshipResponse> emptyPage() {

        return Page.<RelationshipResponse>builder().build();
    }
}
