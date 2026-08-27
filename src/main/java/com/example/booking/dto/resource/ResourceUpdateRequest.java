package com.example.booking.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Request payload to update an existing resource")
public class ResourceUpdateRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Updated resource name", example = "Conference Room A (Renovated)")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Updated description", example = "Modern 12-person conference room with 4K projector")
    private String description;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Schema(description = "Updated type/category", example = "ROOM")
    private String type;

    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be non-negative")
    @Schema(description = "Updated hourly price", example = "1750.00")
    private BigDecimal price;

    @Schema(description = "Updated availability status", example = "true")
    private Boolean available;

    public ResourceUpdateRequest() {
    }

    public ResourceUpdateRequest(String name, String description, String type, BigDecimal price, Boolean available) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
