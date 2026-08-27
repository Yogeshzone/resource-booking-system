package com.example.booking.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Request payload to create a new resource")
public class ResourceCreateRequest {

    @NotBlank(message = "Resource name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Name of the resource", example = "Conference Room A")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed description", example = "10-person conference room with AV equipment")
    private String description;

    @NotBlank(message = "Resource type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Schema(description = "Type/Category of resource", example = "ROOM")
    private String type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be non-negative")
    @Schema(description = "Hourly rental price", example = "1500.00")
    private BigDecimal price;

    @Schema(description = "Initial availability status", example = "true", defaultValue = "true")
    private Boolean available = true;

    public ResourceCreateRequest() {
    }

    public ResourceCreateRequest(String name, String description, String type, BigDecimal price, Boolean available) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.available = available != null ? available : true;
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
