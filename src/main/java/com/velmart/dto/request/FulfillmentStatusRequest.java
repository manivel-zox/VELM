package com.velmart.dto.request;

import com.velmart.model.FulfillmentStatus;
import jakarta.validation.constraints.NotNull;

public class FulfillmentStatusRequest {
    @NotNull(message = "Fulfillment status is required")
    private FulfillmentStatus fulfillmentStatus;

    public FulfillmentStatusRequest() {
    }

    public FulfillmentStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }
}
