package net.smartcosmos.dao.relationships.util;

import net.smartcosmos.dao.relationships.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtil {

    public static Pageable buildPageable(Integer page, Integer size, SortOrder sortOrder, String sortBy)
        throws IllegalArgumentException {

        Sort.Direction direction = Sort.DEFAULT_DIRECTION; // TODO default value to service config
        if (sortOrder != null) {
            direction = RelationshipPersistenceUtil.getSortDirection(sortOrder);
        }
        if (sortBy == null) {
            sortBy = RelationshipPersistenceUtil.getSortByFieldName("created"); // TODO default value to service config
        }
        if (page == null) {
            page = 1; // TODO default value to service config
        }
        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }
        page--;

        if (size == null) {
            size = 20; // TODO default value to service config
        }
        return new PageRequest(page, size, direction, sortBy);
    }
}
