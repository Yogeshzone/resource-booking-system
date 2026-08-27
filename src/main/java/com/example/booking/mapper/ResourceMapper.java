package com.example.booking.mapper;

import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public Resource toEntity(ResourceCreateRequest request) {
        if (request == null) {
            return null;
        }
        return new Resource(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getPrice(),
                request.getAvailable()
        );
    }

    public ResourceResponse toResponseDto(Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getPrice(),
                resource.getAvailable(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
