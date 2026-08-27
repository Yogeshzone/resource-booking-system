package com.example.booking.util;

import com.example.booking.exception.BadRequestException;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class SortUtils {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public static final Set<String> ALLOWED_RESERVATION_SORT_FIELDS = Set.of(
            "id", "startTime", "endTime", "price", "status", "createdAt", "updatedAt"
    );

    public static final Set<String> ALLOWED_RESOURCE_SORT_FIELDS = Set.of(
            "id", "name", "type", "price", "available", "createdAt", "updatedAt"
    );

    private SortUtils() {
    }

    public static Pageable createPageable(Integer page, Integer size, String sortParam,
                                          Set<String> allowedFields, String defaultSortField,
                                          Sort.Direction defaultDirection) {
        int pageNumber = (page != null) ? page : DEFAULT_PAGE;
        int pageSize = (size != null) ? size : DEFAULT_SIZE;

        if (pageNumber < 0) {
            throw new BadRequestException("Page index must not be less than zero");
        }
        if (pageSize < 1) {
            throw new BadRequestException("Page size must not be less than one");
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not exceed " + MAX_PAGE_SIZE);
        }

        Sort sort = parseSort(sortParam, allowedFields, defaultSortField, defaultDirection);
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    public static Sort parseSort(String sortParam, Set<String> allowedFields,
                                 String defaultSortField, Sort.Direction defaultDirection) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Sort.by(defaultDirection != null ? defaultDirection : Sort.Direction.DESC, defaultSortField);
        }

        String[] parts = sortParam.split(",");
        String property = parts[0].trim();

        if (!allowedFields.contains(property)) {
            throw new BadRequestException("Invalid sort field: '" + property + "'. Allowed fields are: " + allowedFields);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dirStr = parts[1].trim().toLowerCase();
            if ("desc".equalsIgnoreCase(dirStr)) {
                direction = Sort.Direction.DESC;
            } else if ("asc".equalsIgnoreCase(dirStr)) {
                direction = Sort.Direction.ASC;
            } else {
                throw new BadRequestException("Invalid sort direction: '" + parts[1] + "'. Must be 'asc' or 'desc'");
            }
        }

        return Sort.by(direction, property);
    }
}
