package com.example.booking.dto.reservation;

import com.example.booking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to update reservation status")
public class ReservationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Target status (CONFIRMED, CANCELLED, etc.)", example = "CANCELLED")
    private ReservationStatus status;

    public ReservationStatusUpdateRequest() {
    }

    public ReservationStatusUpdateRequest(ReservationStatus status) {
        this.status = status;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
