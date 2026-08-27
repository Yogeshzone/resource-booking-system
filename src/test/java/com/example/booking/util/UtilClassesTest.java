package com.example.booking.util;

import com.example.booking.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilClassesTest {

    @Test
    void priceCalculator_CalculatesCorrectHourlyPrice() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 1, 12, 30);
        BigDecimal hourlyRate = new BigDecimal("100.00");

        BigDecimal price = PriceCalculator.calculatePrice(hourlyRate, start, end);
        assertEquals(new BigDecimal("250.00"), price);
    }

    @Test
    void priceCalculator_InvalidArguments_ThrowsException() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 1, 10, 0);
        BigDecimal hourlyRate = new BigDecimal("100.00");

        assertThrows(BadRequestException.class, () -> PriceCalculator.calculatePrice(hourlyRate, start, end));
        assertThrows(IllegalArgumentException.class, () -> PriceCalculator.calculatePrice(null, start, end));
    }

    @Test
    void sortUtils_CreatePageable_Success() {
        Pageable pageable = SortUtils.createPageable(
                0, 10, "price,desc", SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        );
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("price").getDirection());
    }

    @Test
    void sortUtils_InvalidSortField_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> SortUtils.createPageable(
                0, 10, "invalidField,asc", SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        ));
    }

    @Test
    void userEntity_GettersSettersEqualsToString() {
        com.example.booking.entity.User u1 = new com.example.booking.entity.User("alice", "alice@example.com", "pass123", com.example.booking.enums.Role.USER);
        u1.setId(100L);
        u1.setCreatedAt(LocalDateTime.now());
        u1.setUpdatedAt(LocalDateTime.now());

        assertEquals(100L, u1.getId());
        assertEquals("alice", u1.getUsername());
        assertEquals("alice@example.com", u1.getEmail());
        assertEquals("pass123", u1.getPassword());
        assertEquals(com.example.booking.enums.Role.USER, u1.getRole());
        org.junit.jupiter.api.Assertions.assertNotNull(u1.getCreatedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(u1.getUpdatedAt());

        com.example.booking.entity.User u2 = new com.example.booking.entity.User();
        u2.setId(100L);
        u2.setUsername("alice");
        u2.setEmail("alice@example.com");
        u2.setPassword("pass123");
        u2.setRole(com.example.booking.enums.Role.USER);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        org.junit.jupiter.api.Assertions.assertTrue(u1.toString().contains("alice"));
    }

    @Test
    void resourceEntity_GettersSettersEqualsToString() {
        com.example.booking.entity.Resource r1 = new com.example.booking.entity.Resource("Conference Room A", "Large room", "ROOM", new BigDecimal("150.00"), true);
        r1.setId(200L);
        r1.setCreatedAt(LocalDateTime.now());
        r1.setUpdatedAt(LocalDateTime.now());

        assertEquals(200L, r1.getId());
        assertEquals("Conference Room A", r1.getName());
        assertEquals("Large room", r1.getDescription());
        assertEquals("ROOM", r1.getType());
        assertEquals(new BigDecimal("150.00"), r1.getPrice());
        org.junit.jupiter.api.Assertions.assertTrue(r1.getAvailable());

        com.example.booking.entity.Resource r2 = new com.example.booking.entity.Resource();
        r2.setId(200L);
        r2.setName("Conference Room A");
        r2.setDescription("Large room");
        r2.setType("ROOM");
        r2.setPrice(new BigDecimal("150.00"));
        r2.setAvailable(true);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        org.junit.jupiter.api.Assertions.assertTrue(r1.toString().contains("Conference Room A"));
    }

    @Test
    void reservationEntity_GettersSettersEqualsToString() {
        com.example.booking.entity.User user = new com.example.booking.entity.User("bob", "bob@example.com", "pass", com.example.booking.enums.Role.USER);
        user.setId(1L);
        com.example.booking.entity.Resource res = new com.example.booking.entity.Resource("Projector", "4K", "EQUIPMENT", new BigDecimal("50.00"), true);
        res.setId(2L);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        com.example.booking.entity.Reservation resv1 = new com.example.booking.entity.Reservation(res, user, start, end, new BigDecimal("100.00"), com.example.booking.enums.ReservationStatus.PENDING);
        resv1.setId(300L);
        resv1.setCreatedAt(LocalDateTime.now());
        resv1.setUpdatedAt(LocalDateTime.now());

        assertEquals(300L, resv1.getId());
        assertEquals(res, resv1.getResource());
        assertEquals(user, resv1.getUser());
        assertEquals(start, resv1.getStartTime());
        assertEquals(end, resv1.getEndTime());
        assertEquals(new BigDecimal("100.00"), resv1.getPrice());
        assertEquals(com.example.booking.enums.ReservationStatus.PENDING, resv1.getStatus());

        com.example.booking.entity.Reservation resv2 = new com.example.booking.entity.Reservation();
        resv2.setId(300L);
        resv2.setResource(res);
        resv2.setUser(user);
        resv2.setStartTime(start);
        resv2.setEndTime(end);
        resv2.setPrice(new BigDecimal("100.00"));
        resv2.setStatus(com.example.booking.enums.ReservationStatus.PENDING);

        assertEquals(resv1, resv2);
        assertEquals(resv1.hashCode(), resv2.hashCode());
        org.junit.jupiter.api.Assertions.assertTrue(resv1.toString().contains("300"));
    }

    @Test
    void dtosAndExceptions_ComprehensiveCoverage() {
        com.example.booking.dto.auth.LoginResponse auth = new com.example.booking.dto.auth.LoginResponse("jwt-tok", "Bearer", 3600L, "user1", "USER");
        assertEquals("jwt-tok", auth.getToken());
        assertEquals("Bearer", auth.getTokenType());
        assertEquals(3600L, auth.getExpiresIn());
        assertEquals("user1", auth.getUsername());
        assertEquals("USER", auth.getRole());

        com.example.booking.dto.auth.LoginRequest login = new com.example.booking.dto.auth.LoginRequest("user1", "pass1");
        assertEquals("user1", login.getUsername());
        assertEquals("pass1", login.getPassword());

        com.example.booking.dto.common.ErrorResponse err = new com.example.booking.dto.common.ErrorResponse(
                400, "Bad Request", "Invalid input", "/api/test", java.util.Map.of("field", "must not be blank")
        );
        assertEquals(400, err.getStatus());
        assertEquals("Bad Request", err.getError());
        assertEquals("Invalid input", err.getMessage());
        assertEquals("/api/test", err.getPath());
        org.junit.jupiter.api.Assertions.assertNotNull(err.getFieldErrors());

        com.example.booking.dto.common.PagedResponse<String> paged = com.example.booking.dto.common.PagedResponse.<String>builder()
                .content(java.util.List.of("A", "B"))
                .page(0)
                .size(10)
                .totalElements(2L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        assertEquals(2, paged.getContent().size());
        assertEquals(0, paged.getPage());
        assertEquals(10, paged.getSize());
        assertEquals(2L, paged.getTotalElements());
        assertEquals(1, paged.getTotalPages());
        org.junit.jupiter.api.Assertions.assertTrue(paged.isFirst());
        org.junit.jupiter.api.Assertions.assertTrue(paged.isLast());

        com.example.booking.dto.user.UserSummaryDto userSum = new com.example.booking.dto.user.UserSummaryDto(1L, "admin", "admin@example.com", com.example.booking.enums.Role.ADMIN);
        assertEquals(1L, userSum.getId());
        assertEquals("admin", userSum.getUsername());
        assertEquals(com.example.booking.enums.Role.ADMIN, userSum.getRole());

        com.example.booking.dto.resource.ResourceCreateRequest rcr = new com.example.booking.dto.resource.ResourceCreateRequest("Room", "Desc", "ROOM", new BigDecimal("100"), true);
        assertEquals("Room", rcr.getName());
        assertEquals("Desc", rcr.getDescription());
        assertEquals("ROOM", rcr.getType());
        assertEquals(new BigDecimal("100"), rcr.getPrice());
        org.junit.jupiter.api.Assertions.assertTrue(rcr.getAvailable());

        com.example.booking.dto.resource.ResourceUpdateRequest rur = new com.example.booking.dto.resource.ResourceUpdateRequest();
        rur.setName("New Room");
        rur.setDescription("New Desc");
        rur.setType("EQUIPMENT");
        rur.setPrice(new BigDecimal("200"));
        rur.setAvailable(false);
        assertEquals("New Room", rur.getName());
        assertEquals("New Desc", rur.getDescription());
        assertEquals("EQUIPMENT", rur.getType());
        assertEquals(new BigDecimal("200"), rur.getPrice());
        org.junit.jupiter.api.Assertions.assertFalse(rur.getAvailable());

        com.example.booking.dto.resource.ResourceResponse rr = new com.example.booking.dto.resource.ResourceResponse();
        rr.setId(1L);
        rr.setName("Room");
        assertEquals(1L, rr.getId());
        assertEquals("Room", rr.getName());

        com.example.booking.dto.reservation.ReservationCreateRequest req = new com.example.booking.dto.reservation.ReservationCreateRequest(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        assertEquals(1L, req.getResourceId());

        com.example.booking.dto.reservation.AdminReservationCreateRequest adminReq = new com.example.booking.dto.reservation.AdminReservationCreateRequest(1L, 2L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), com.example.booking.enums.ReservationStatus.CONFIRMED);
        assertEquals(1L, adminReq.getUserId());
        assertEquals(2L, adminReq.getResourceId());
        assertEquals(com.example.booking.enums.ReservationStatus.CONFIRMED, adminReq.getStatus());

        com.example.booking.dto.reservation.ReservationUpdateRequest rup = new com.example.booking.dto.reservation.ReservationUpdateRequest();
        rup.setStartTime(LocalDateTime.now());
        rup.setEndTime(LocalDateTime.now().plusHours(2));
        rup.setStatus(com.example.booking.enums.ReservationStatus.CANCELLED);
        org.junit.jupiter.api.Assertions.assertNotNull(rup.getStartTime());
        org.junit.jupiter.api.Assertions.assertNotNull(rup.getEndTime());
        assertEquals(com.example.booking.enums.ReservationStatus.CANCELLED, rup.getStatus());

        com.example.booking.dto.reservation.ReservationResponse resResp = new com.example.booking.dto.reservation.ReservationResponse();
        resResp.setId(1L);
        resResp.setStatus(com.example.booking.enums.ReservationStatus.PENDING);
        assertEquals(1L, resResp.getId());
        assertEquals(com.example.booking.enums.ReservationStatus.PENDING, resResp.getStatus());

        com.example.booking.dto.reservation.ReservationFilterRequest filter = new com.example.booking.dto.reservation.ReservationFilterRequest(
                com.example.booking.enums.ReservationStatus.CONFIRMED, new BigDecimal("10"), new BigDecimal("100"), 1L, 2L, 0, 10, "createdAt,desc"
        );
        assertEquals(com.example.booking.enums.ReservationStatus.CONFIRMED, filter.getStatus());
        assertEquals(new BigDecimal("10"), filter.getMinPrice());
        assertEquals(new BigDecimal("100"), filter.getMaxPrice());
        assertEquals(1L, filter.getResourceId());
        assertEquals(2L, filter.getUserId());
        assertEquals(0, filter.getPage());
        assertEquals(10, filter.getSize());
        assertEquals("createdAt,desc", filter.getSort());

        com.example.booking.dto.reservation.ReservationFilterRequest emptyFilter = new com.example.booking.dto.reservation.ReservationFilterRequest();
        emptyFilter.setStatus(com.example.booking.enums.ReservationStatus.PENDING);
        emptyFilter.setMinPrice(new BigDecimal("20"));
        emptyFilter.setMaxPrice(new BigDecimal("200"));
        emptyFilter.setResourceId(5L);
        emptyFilter.setUserId(6L);
        emptyFilter.setPage(1);
        emptyFilter.setSize(20);
        emptyFilter.setSort("price,asc");
        assertEquals(com.example.booking.enums.ReservationStatus.PENDING, emptyFilter.getStatus());
        assertEquals(new BigDecimal("20"), emptyFilter.getMinPrice());
        assertEquals(new BigDecimal("200"), emptyFilter.getMaxPrice());
        assertEquals(5L, emptyFilter.getResourceId());
        assertEquals(6L, emptyFilter.getUserId());
        assertEquals(1, emptyFilter.getPage());
        assertEquals(20, emptyFilter.getSize());
        assertEquals("price,asc", emptyFilter.getSort());

        com.example.booking.dto.resource.ResourceFilterRequest resFilter = new com.example.booking.dto.resource.ResourceFilterRequest(
                "ROOM", true, new BigDecimal("50"), new BigDecimal("500"), "test", 0, 10, "id,asc"
        );
        assertEquals("ROOM", resFilter.getType());
        assertTrue(resFilter.getAvailable());
        assertEquals(new BigDecimal("50"), resFilter.getMinPrice());
        assertEquals(new BigDecimal("500"), resFilter.getMaxPrice());
        assertEquals("test", resFilter.getSearch());
        assertEquals(0, resFilter.getPage());
        assertEquals(10, resFilter.getSize());
        assertEquals("id,asc", resFilter.getSort());

        com.example.booking.dto.resource.ResourceFilterRequest emptyResFilter = new com.example.booking.dto.resource.ResourceFilterRequest();
        emptyResFilter.setType("VEHICLE");
        emptyResFilter.setAvailable(false);
        emptyResFilter.setMinPrice(new BigDecimal("100"));
        emptyResFilter.setMaxPrice(new BigDecimal("1000"));
        emptyResFilter.setSearch("car");
        emptyResFilter.setPage(2);
        emptyResFilter.setSize(50);
        emptyResFilter.setSort("price,desc");
        assertEquals("VEHICLE", emptyResFilter.getType());
        assertFalse(emptyResFilter.getAvailable());
        assertEquals(new BigDecimal("100"), emptyResFilter.getMinPrice());
        assertEquals(new BigDecimal("1000"), emptyResFilter.getMaxPrice());
        assertEquals("car", emptyResFilter.getSearch());
        assertEquals(2, emptyResFilter.getPage());
        assertEquals(50, emptyResFilter.getSize());
        assertEquals("price,desc", emptyResFilter.getSort());

        // Exceptions
        assertEquals("msg", new com.example.booking.exception.InvalidStatusTransitionException("msg").getMessage());
        assertEquals("msg", new com.example.booking.exception.ReservationConflictException("msg").getMessage());
        assertEquals("msg", new com.example.booking.exception.ReservationNotFoundException("msg").getMessage());
        org.junit.jupiter.api.Assertions.assertTrue(new com.example.booking.exception.ReservationNotFoundException(99L).getMessage().contains("99"));
        assertEquals("msg", new com.example.booking.exception.ResourceInUseException("msg").getMessage());
        assertEquals("msg", new com.example.booking.exception.ResourceNotFoundException("msg").getMessage());
        org.junit.jupiter.api.Assertions.assertTrue(new com.example.booking.exception.ResourceNotFoundException(42L).getMessage().contains("42"));
        assertEquals("msg", new com.example.booking.exception.UserNotFoundException("msg").getMessage());
        org.junit.jupiter.api.Assertions.assertTrue(new com.example.booking.exception.UserNotFoundException(77L).getMessage().contains("77"));
    }

    @Test
    void specifications_ValidationAndFilteringChecks() {
        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ReservationSpecification.withFilters(
                null, new BigDecimal("200"), new BigDecimal("100"), null, null
        ));
        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ReservationSpecification.withFilters(
                null, new BigDecimal("-10"), null, null, null
        ));
        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ReservationSpecification.withFilters(
                null, null, new BigDecimal("-10"), null, null
        ));
        org.junit.jupiter.api.Assertions.assertNotNull(com.example.booking.specification.ReservationSpecification.withFilters(
                com.example.booking.enums.ReservationStatus.CONFIRMED, new BigDecimal("50"), new BigDecimal("500"), 1L, 2L
        ));

        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ResourceSpecification.withFilters(
                null, null, new BigDecimal("200"), new BigDecimal("100"), null
        ));
        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ResourceSpecification.withFilters(
                null, null, new BigDecimal("-10"), null, null
        ));
        assertThrows(BadRequestException.class, () -> com.example.booking.specification.ResourceSpecification.withFilters(
                null, null, null, new BigDecimal("-10"), null
        ));
        org.junit.jupiter.api.Assertions.assertNotNull(com.example.booking.specification.ResourceSpecification.withFilters(
                "ROOM", true, new BigDecimal("50"), new BigDecimal("500"), "test"
        ));
    }
}