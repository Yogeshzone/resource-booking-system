package com.example.booking.mapper;

import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;

    public ReservationMapper(ResourceMapper resourceMapper, UserMapper userMapper) {
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
    }

    public ReservationResponse toResponseDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationResponse(
                reservation.getId(),
                resourceMapper.toResponseDto(reservation.getResource()),
                userMapper.toSummaryDto(reservation.getUser()),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
