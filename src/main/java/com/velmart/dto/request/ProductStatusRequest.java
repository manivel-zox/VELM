package com.velmart.dto.request;

import jakarta.validation.constraints.Min;

public class ProductStatusRequest {
    private Boolean active;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 1, message = "Price must be greater than 0")
    private Double price;

    public ProductStatusRequest() {
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
