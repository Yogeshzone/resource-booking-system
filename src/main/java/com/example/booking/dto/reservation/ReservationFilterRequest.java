package com.example.booking.dto.reservation;

import com.example.booking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Filter and pagination parameters for reservation queries")
public class ReservationFilterRequest {

    @Schema(description = "Filter by status (PENDING, CONFIRMED, CANCELLED)", example = "CONFIRMED")
    private ReservationStatus status;

    @Schema(description = "Filter by minimum total price", example = "50.00")
    private BigDecimal minPrice;

    @Schema(description = "Filter by maximum total price", example = "500.00")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by resource ID", example = "1")
    private Long resourceId;

    @Schema(description = "Filter by target user ID (ADMIN role only; ignored for USER)", example = "2")
    private Long userId;

    @Schema(description = "Zero-indexed page number (default: 0)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size (default: 10, max: 100)", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort expression (e.g. 'startTime,asc', 'price,desc')", example = "createdAt,desc")
    private String sort = "createdAt,desc";

    public ReservationFilterRequest() {
    }

    public ReservationFilterRequest(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long resourceId,
            Long userId,
            Integer page,
            Integer size,
            String sort) {
        this.status = status;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.resourceId = resourceId;
        this.userId = userId;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 10;
        this.sort = sort != null ? sort : "createdAt,desc";
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null ? page : 0;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size != null ? size : 10;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort != null ? sort : "createdAt,desc";
    }
}
