package com.example.booking.dto.reservation;

import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.dto.user.UserSummaryDto;
import com.example.booking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Reservation response details")
public class ReservationResponse {

    @Schema(description = "Reservation ID", example = "1")
    private Long id;

    @Schema(description = "Booked Resource details")
    private ResourceResponse resource;

    @Schema(description = "User who owns the reservation")
    private UserSummaryDto user;

    @Schema(description = "Booking start time")
    private LocalDateTime startTime;

    @Schema(description = "Booking end time")
    private LocalDateTime endTime;

    @Schema(description = "Calculated total price for the booking duration", example = "3000.00")
    private BigDecimal price;

    @Schema(description = "Reservation status", example = "CONFIRMED")
    private ReservationStatus status;

    @Schema(description = "Reservation creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public ReservationResponse() {
    }

    public ReservationResponse(Long id, ResourceResponse resource, UserSummaryDto user,
                               LocalDateTime startTime, LocalDateTime endTime, BigDecimal price,
                               ReservationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.resource = resource;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceResponse getResource() {
        return resource;
    }

    public void setResource(ResourceResponse resource) {
        this.resource = resource;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
