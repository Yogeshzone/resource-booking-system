package com.example.booking.util;

import com.example.booking.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public final class PriceCalculator {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    private PriceCalculator() {
    }

    public static BigDecimal calculatePrice(BigDecimal hourlyRate, LocalDateTime startTime, LocalDateTime endTime) {
        if (hourlyRate == null) {
            throw new IllegalArgumentException("Hourly rate cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes <= 0) {
            throw new BadRequestException("Booking duration must be greater than zero minutes");
        }

        BigDecimal hours = BigDecimal.valueOf(durationMinutes)
                .divide(MINUTES_PER_HOUR, 4, RoundingMode.HALF_UP);

        return hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
