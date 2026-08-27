package com.example.booking.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "User request to create a reservation. Ownership is always derived from the authenticated JWT.")
public class ReservationCreateRequest {

    @NotNull(message = "Resource ID is required")
    @Schema(description = "ID of the resource to book", example = "1")
    private Long resourceId;

    @NotNull(message = "Start time is required")
    @Schema(description = "Booking start time (ISO-8601)", example = "2026-09-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "Booking end time (ISO-8601)", example = "2026-09-01T12:00:00")
    private LocalDateTime endTime;

    public ReservationCreateRequest() {
    }

    public ReservationCreateRequest(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
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
}
