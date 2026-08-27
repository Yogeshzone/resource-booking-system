package com.example.booking.dto.reservation;

import com.example.booking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Admin request payload to update an existing reservation")
public class ReservationUpdateRequest {

    @Schema(description = "Updated booking start time", example = "2026-09-01T11:00:00")
    private LocalDateTime startTime;

    @Schema(description = "Updated booking end time", example = "2026-09-01T13:00:00")
    private LocalDateTime endTime;

    @Schema(description = "Updated reservation status", example = "CONFIRMED")
    private ReservationStatus status;

    public ReservationUpdateRequest() {
    }

    public ReservationUpdateRequest(LocalDateTime startTime, LocalDateTime endTime, ReservationStatus status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
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
