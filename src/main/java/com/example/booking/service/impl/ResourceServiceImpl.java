package com.example.booking.service.impl;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.dto.resource.ResourceUpdateRequest;
import com.example.booking.entity.Resource;
import com.example.booking.enums.ReservationStatus;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceInUseException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.mapper.ResourceMapper;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.service.ResourceService;
import com.example.booking.specification.ResourceSpecification;
import com.example.booking.util.SortUtils;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final ResourceMapper resourceMapper;

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            ReservationRepository reservationRepository,
            ResourceMapper resourceMapper) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.resourceMapper = resourceMapper;
    }

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request) {
        log.info("Creating new resource: name='{}', type='{}', price={}", request.getName(), request.getType(), request.getPrice());
        Resource resource = resourceMapper.toEntity(request);
        Resource saved = resourceRepository.save(resource);
        return resourceMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return resourceMapper.toResponseDto(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> getAllResources(
            String type,
            Boolean available,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Integer page,
            Integer size,
            String sort) {

        Pageable pageable = SortUtils.createPageable(
                page,
                size,
                sort,
                SortUtils.ALLOWED_RESOURCE_SORT_FIELDS,
                "createdAt",
                Sort.Direction.DESC
        );

        Specification<Resource> spec = ResourceSpecification.withFilters(type, available, minPrice, maxPrice, search);
        Page<Resource> resourcePage = resourceRepository.findAll(spec, pageable);

        List<ResourceResponse> responses = resourcePage.getContent().stream()
                .map(resourceMapper::toResponseDto)
                .toList();

        return new PagedResponse<>(
                responses,
                resourcePage.getNumber(),
                resourcePage.getSize(),
                resourcePage.getTotalElements(),
                resourcePage.getTotalPages(),
                resourcePage.isFirst(),
                resourcePage.isLast()
        );
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceUpdateRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new BadRequestException("Resource name cannot be empty");
            }
            resource.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription().trim());
        }

        if (request.getType() != null) {
            if (request.getType().trim().isEmpty()) {
                throw new BadRequestException("Resource type cannot be empty");
            }
            resource.setType(request.getType().trim());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Price must be non-negative");
            }
            resource.setPrice(request.getPrice());
        }

        if (request.getAvailable() != null) {
            resource.setAvailable(request.getAvailable());
        }

        Resource updated = resourceRepository.save(resource);
        log.info("Updated resource with ID: {}", id);
        return resourceMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public ResourceResponse patchResource(Long id, ResourceUpdateRequest request) {
        return updateResource(id, request);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        long activeReservations = reservationRepository.countByResourceIdAndStatusIn(
                id, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        if (activeReservations > 0) {
            throw new ResourceInUseException(
                    String.format("Cannot delete resource ID %d because it has %d active reservation(s)", id, activeReservations));
        }

        resourceRepository.delete(resource);
        log.info("Successfully deleted resource with ID: {}", id);
    }
}
