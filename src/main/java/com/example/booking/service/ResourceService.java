package com.example.booking.service;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.dto.resource.ResourceUpdateRequest;
import java.math.BigDecimal;

public interface ResourceService {

    ResourceResponse createResource(ResourceCreateRequest request);

    ResourceResponse getResourceById(Long id);

    PagedResponse<ResourceResponse> getAllResources(
            String type,
            Boolean available,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Integer page,
            Integer size,
            String sort
    );

    ResourceResponse updateResource(Long id, ResourceUpdateRequest request);

    ResourceResponse patchResource(Long id, ResourceUpdateRequest request);

    void deleteResource(Long id);
}
