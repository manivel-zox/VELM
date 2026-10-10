package com.velmart.dto.request;

import com.velmart.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CheckoutRequest {
    @NotBlank(message = "Delivery address ID is required")
    private String addressId;

    @NotBlank(message = "Contact phone number is required")
    @Pattern(regexp = "^[0-9+ -]{10,15}$", message = "Phone number must be valid (10-15 digits)")
    private String contactPhone;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    private String idempotencyKey;

    public CheckoutRequest() {
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
