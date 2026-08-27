package com.example.booking.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Filter and pagination parameters for resource queries")
public class ResourceFilterRequest {

    @Schema(description = "Filter by resource type (e.g. ROOM, VEHICLE, EQUIPMENT)", example = "ROOM")
    private String type;

    @Schema(description = "Filter by availability status", example = "true")
    private Boolean available;

    @Schema(description = "Filter by minimum price", example = "50.00")
    private BigDecimal minPrice;

    @Schema(description = "Filter by maximum price", example = "500.00")
    private BigDecimal maxPrice;

    @Schema(description = "Case-insensitive keyword search on resource name and description", example = "conference")
    private String search;

    @Schema(description = "Zero-indexed page number (default: 0)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size (default: 10, max: 100)", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort expression (e.g. 'name,asc', 'price,desc')", example = "id,asc")
    private String sort = "id,asc";

    public ResourceFilterRequest() {
    }

    public ResourceFilterRequest(
            String type,
            Boolean available,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Integer page,
            Integer size,
            String sort) {
        this.type = type;
        this.available = available;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.search = search;
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}