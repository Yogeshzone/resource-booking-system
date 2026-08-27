package com.example.booking.dto.reservation;

import com.example.booking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Admin request to create a reservation on behalf of any user")
public class AdminReservationCreateRequest {

    @Schema(description = "ID of the target user (defaults to current admin if omitted)", example = "2")
    private Long userId;

    @NotNull(message = "Resource ID is required")
    @Schema(description = "ID of the resource to book", example = "1")
    private Long resourceId;

    @NotNull(message = "Start time is required")
    @Schema(description = "Booking start time (ISO-8601)", example = "2026-09-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "Booking end time (ISO-8601)", example = "2026-09-01T12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "Initial status (defaults to PENDING if omitted)", example = "CONFIRMED")
    private ReservationStatus status;

    public AdminReservationCreateRequest() {
    }

    public AdminReservationCreateRequest(Long userId, Long resourceId, LocalDateTime startTime,
                                         LocalDateTime endTime, ReservationStatus status) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
