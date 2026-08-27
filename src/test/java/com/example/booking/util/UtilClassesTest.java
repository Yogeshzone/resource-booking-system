package com.example.booking.util;

import com.example.booking.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilClassesTest {

    @Test
    void priceCalculator_CalculatesCorrectHourlyPrice() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 1, 12, 30);
        BigDecimal hourlyRate = new BigDecimal("100.00");

        BigDecimal price = PriceCalculator.calculatePrice(hourlyRate, start, end);
        assertEquals(new BigDecimal("250.00"), price);
    }

    @Test
    void priceCalculator_InvalidArguments_ThrowsException() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 1, 10, 0);
        BigDecimal hourlyRate = new BigDecimal("100.00");

        assertThrows(BadRequestException.class, () -> PriceCalculator.calculatePrice(hourlyRate, start, end));
        assertThrows(IllegalArgumentException.class, () -> PriceCalculator.calculatePrice(null, start, end));
    }

    @Test
    void sortUtils_CreatePageable_Success() {
        Pageable pageable = SortUtils.createPageable(
                0, 10, "price,desc", SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        );
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("price").getDirection());
    }

    @Test
    void sortUtils_InvalidSortField_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> SortUtils.createPageable(
                0, 10, "invalidField,asc", SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        ));
    }

    @Test
    void sortUtils_InvalidPageParams_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> SortUtils.createPageable(
                -1, 10, null, SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        ));
        assertThrows(BadRequestException.class, () -> SortUtils.createPageable(
                0, 0, null, SortUtils.ALLOWED_RESERVATION_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        ));
    }
}