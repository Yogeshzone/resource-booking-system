package com.example.booking.service;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceFilterRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.dto.resource.ResourceUpdateRequest;

public interface ResourceService {

    ResourceResponse createResource(ResourceCreateRequest request);

    ResourceResponse getResourceById(Long id);

    PagedResponse<ResourceResponse> getAllResources(ResourceFilterRequest filter);

    ResourceResponse updateResource(Long id, ResourceUpdateRequest request);

    ResourceResponse patchResource(Long id, ResourceUpdateRequest request);

    void deleteResource(Long id);
}
