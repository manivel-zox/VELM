package com.velmart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_seller", columnList = "seller_id")
})
public class OrderItem {

    @Id
    @Column(name = "order_item_id", length = 64, nullable = false)
    private String orderItemId;

    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;

    @Column(name = "seller_id", length = 64, nullable = false)
    private String sellerId;

    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "product_image_snapshot", length = 1000)
    private String productImageSnapshot;

    @Column(name = "unit_price_snapshot")
    private Double unitPriceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total")
    private Double lineTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", length = 32)
    private FulfillmentStatus fulfillmentStatus;

    @Column(name = "created_at")
    private String createdAt;

    public OrderItem() {
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public String getProductImageSnapshot() {
        return productImageSnapshot;
    }

    public void setProductImageSnapshot(String productImageSnapshot) {
        this.productImageSnapshot = productImageSnapshot;
    }

    public Double getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(Double unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(Double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public FulfillmentStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
