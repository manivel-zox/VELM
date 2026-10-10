package com.velmart.dto.response;

import com.velmart.model.OrderItem;
import com.velmart.model.OrderStatus;
import com.velmart.model.PaymentMethod;
import com.velmart.model.PaymentStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDetailDto {
    private String orderId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private String contactPhone;
    private OrderStatus orderStatus;
    private Double itemSubtotal;
    private Double shippingFee;
    private Double totalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Map<String, String> deliveryAddressSnapshot;
    private String cancellationReason;
    private String createdAt;
    private String updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public OrderDetailDto() {
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
