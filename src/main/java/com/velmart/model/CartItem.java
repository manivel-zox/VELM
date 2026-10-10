package com.velmart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_user", columnList = "user_id"),
        @Index(name = "idx_cart_user_product", columnList = "user_id, product_id")
})
public class CartItem {

    @Id
    @Column(name = "cart_item_id", length = 128, nullable = false)
    private String cartItemId;

    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "updated_at")
    private String updatedAt;

    public CartItem() {
    }

    public CartItem(String userId, String productId, Integer quantity, String updatedAt) {
        this.cartItemId = userId + "#" + productId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
