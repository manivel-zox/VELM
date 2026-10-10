package com.velmart.dto.response;

import java.util.ArrayList;
import java.util.List;

public class CartViewDto {
    private List<CartItemViewDto> items = new ArrayList<>();
    private int totalItems = 0;
    private double itemSubtotal = 0.0;
    private double shippingFee = 0.0;
    private double totalAmount = 0.0;
    private boolean allInStock = true;

    public CartViewDto() {
    }

    public List<CartItemViewDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemViewDto> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getItemSubtotal() {
        return itemSubtotal;
    }

    public void setItemSubtotal(double itemSubtotal) {
        this.itemSubtotal = itemSubtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isAllInStock() {
        return allInStock;
    }

    public void setAllInStock(boolean allInStock) {
        this.allInStock = allInStock;
    }
}
