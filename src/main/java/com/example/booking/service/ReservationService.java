package com.example.booking.service;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.reservation.AdminReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.dto.reservation.ReservationUpdateRequest;
import com.example.booking.enums.ReservationStatus;
import java.math.BigDecimal;

public interface ReservationService {

    ReservationResponse createReservation(ReservationCreateRequest request);

    ReservationResponse createAdminReservation(AdminReservationCreateRequest request);

    ReservationResponse getReservationById(Long id);

    PagedResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long resourceId,
            Long userId,
            Integer page,
            Integer size,
            String sort
    );

    ReservationResponse cancelReservation(Long id);

    ReservationResponse updateReservation(Long id, ReservationUpdateRequest request);

    ReservationResponse updateReservationStatus(Long id, ReservationStatus status);

    void deleteReservation(Long id);
}
