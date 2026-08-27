package com.example.booking.service;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.reservation.AdminReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationFilterRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.dto.reservation.ReservationUpdateRequest;
import com.example.booking.enums.ReservationStatus;

public interface ReservationService {

    ReservationResponse createReservation(ReservationCreateRequest request);

    ReservationResponse createAdminReservation(AdminReservationCreateRequest request);

    ReservationResponse getReservationById(Long id);

    PagedResponse<ReservationResponse> getAllReservations(ReservationFilterRequest filter);

    ReservationResponse cancelReservation(Long id);

    ReservationResponse updateReservation(Long id, ReservationUpdateRequest request);

    ReservationResponse updateReservationStatus(Long id, ReservationStatus status);

    void deleteReservation(Long id);
}
