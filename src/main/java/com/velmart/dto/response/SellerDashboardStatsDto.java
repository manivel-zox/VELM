package com.velmart.dto.response;

public class SellerDashboardStatsDto {
    private int totalProducts;
    private int activeProducts;
    private int outOfStockProducts;
    private int totalOrders;
    private int pendingFulfillments;
    private double totalSalesRevenue;

    public SellerDashboardStatsDto() {
    }

    public SellerDashboardStatsDto(int totalProducts, int activeProducts, int outOfStockProducts, int totalOrders, int pendingFulfillments, double totalSalesRevenue) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.outOfStockProducts = outOfStockProducts;
        this.totalOrders = totalOrders;
        this.pendingFulfillments = pendingFulfillments;
        this.totalSalesRevenue = totalSalesRevenue;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(int activeProducts) {
        this.activeProducts = activeProducts;
    }

    public int getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(int outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getPendingFulfillments() {
        return pendingFulfillments;
    }

    public void setPendingFulfillments(int pendingFulfillments) {
        this.pendingFulfillments = pendingFulfillments;
    }

    public double getTotalSalesRevenue() {
        return totalSalesRevenue;
    }

    public void setTotalSalesRevenue(double totalSalesRevenue) {
        this.totalSalesRevenue = totalSalesRevenue;
    }
}
