package com.velmart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartUpdateRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or greater (0 removes item)")
    private Integer quantity;

    public CartUpdateRequest() {
    }

    public CartUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
