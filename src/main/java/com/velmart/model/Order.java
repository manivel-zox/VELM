package com.velmart.model;

import com.velmart.util.StringMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_order_idempotency", columnList = "idempotency_key")
})
public class Order {

    @Id
    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;

    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 32)
    private OrderStatus orderStatus;

    @Column(name = "item_subtotal")
    private Double itemSubtotal;

    @Column(name = "shipping_fee")
    private Double shippingFee;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 32)
    private PaymentStatus paymentStatus;

    @Convert(converter = StringMapConverter.class)
    @Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
    private Map<String, String> deliveryAddressSnapshot = new HashMap<>();

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getItemSubtotal() {
        return itemSubtotal;
    }

    public void setItemSubtotal(Double itemSubtotal) {
        this.itemSubtotal = itemSubtotal;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Map<String, String> getDeliveryAddressSnapshot() {
        return deliveryAddressSnapshot;
    }

    public void setDeliveryAddressSnapshot(Map<String, String> deliveryAddressSnapshot) {
        this.deliveryAddressSnapshot = deliveryAddressSnapshot;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
