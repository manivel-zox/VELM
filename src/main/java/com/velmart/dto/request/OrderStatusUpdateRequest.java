package com.velmart.dto.request;

import com.velmart.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    private String cancellationReason;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
