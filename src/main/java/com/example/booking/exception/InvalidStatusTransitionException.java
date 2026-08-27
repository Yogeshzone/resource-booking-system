package com.example.booking.exception;

import com.example.booking.enums.ReservationStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ReservationStatus currentStatus, ReservationStatus targetStatus) {
        super(String.format("Cannot transition reservation status from %s to %s", currentStatus, targetStatus));
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
