package com.example.booking.enums;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED;

    public boolean isBlocking() {
        return this == PENDING || this == CONFIRMED;
    }
}
