package com.example.booking.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(Long reservationId) {
        super("Reservation not found with ID: " + reservationId);
    }
}
