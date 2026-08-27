package com.example.booking.enums;

import java.util.List;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED;

    public static final List<ReservationStatus> BLOCKING_STATUSES = List.of(PENDING, CONFIRMED);

    public boolean isBlocking() {
        return this == PENDING || this == CONFIRMED;
    }
}
