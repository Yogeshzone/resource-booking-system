package com.example.booking.service;

import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.entity.Resource;
import com.example.booking.exception.ResourceInUseException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.mapper.ResourceMapper;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.service.impl.ResourceServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private ResourceServiceImpl resourceService;
    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        ResourceMapper resourceMapper = new ResourceMapper();
        resourceService = new ResourceServiceImpl(resourceRepository, reservationRepository, resourceMapper);

        sampleResource = new Resource("Test Room", "Description", "ROOM", new BigDecimal("100.00"), true);
        sampleResource.setId(1L);
    }

    @Test
    void createResource_Success() {
        ResourceCreateRequest request = new ResourceCreateRequest("Test Room", "Description", "ROOM", new BigDecimal("100.00"), true);

        when(resourceRepository.save(any(Resource.class))).thenReturn(sampleResource);

        ResourceResponse result = resourceService.createResource(request);

        assertNotNull(result);
        assertEquals("Test Room", result.getName());
        assertEquals("ROOM", result.getType());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void getResourceById_NotFound_ThrowsException() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(999L));
    }

    @Test
    void deleteResource_WithActiveReservations_ThrowsResourceInUseException() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));
        when(reservationRepository.countByResourceIdAndStatusIn(eq(1L), anyList())).thenReturn(2L);

        assertThrows(ResourceInUseException.class, () -> resourceService.deleteResource(1L));
    }

    @Test
    void deleteResource_NoActiveReservations_Success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));
        when(reservationRepository.countByResourceIdAndStatusIn(eq(1L), anyList())).thenReturn(0L);

        resourceService.deleteResource(1L);

        verify(resourceRepository).delete(sampleResource);
    }

    @Test
    void getAllResources_WithFilter_ReturnsPagedResponse() {
        com.example.booking.dto.resource.ResourceFilterRequest filter =
                new com.example.booking.dto.resource.ResourceFilterRequest();
        filter.setType("ROOM");

        org.springframework.data.domain.Page<Resource> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(resourceRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        com.example.booking.dto.common.PagedResponse<ResourceResponse> response =
                resourceService.getAllResources(filter);

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void updateResource_Success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        com.example.booking.dto.resource.ResourceUpdateRequest request =
                new com.example.booking.dto.resource.ResourceUpdateRequest(
                        "Updated Room", "Updated Desc", "ROOM", new BigDecimal("150.00"), false
                );

        ResourceResponse response = resourceService.updateResource(1L, request);
        assertNotNull(response);
        assertEquals("Updated Room", response.getName());
        assertEquals("Updated Desc", response.getDescription());
        assertEquals(new BigDecimal("150.00"), response.getPrice());
    }

    @Test
    void patchResource_PartialUpdate_Success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        com.example.booking.dto.resource.ResourceUpdateRequest request =
                new com.example.booking.dto.resource.ResourceUpdateRequest(
                        null, "Patched Desc", null, null, null
                );

        ResourceResponse response = resourceService.patchResource(1L, request);
        assertNotNull(response);
        assertEquals("Test Room", response.getName());
        assertEquals("Patched Desc", response.getDescription());
    }

    @Test
    void updateResource_EmptyName_ThrowsBadRequestException() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));

        com.example.booking.dto.resource.ResourceUpdateRequest request =
                new com.example.booking.dto.resource.ResourceUpdateRequest(
                        "   ", "Desc", "ROOM", new BigDecimal("100.00"), true
                );

        assertThrows(com.example.booking.exception.BadRequestException.class, () -> resourceService.updateResource(1L, request));
    }

    @Test
    void updateResource_NegativePrice_ThrowsBadRequestException() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));

        com.example.booking.dto.resource.ResourceUpdateRequest request =
                new com.example.booking.dto.resource.ResourceUpdateRequest(
                        "Name", "Desc", "ROOM", new BigDecimal("-10.00"), true
                );

        assertThrows(com.example.booking.exception.BadRequestException.class, () -> resourceService.updateResource(1L, request));
    }
}
