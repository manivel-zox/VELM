package com.velmart.dto.response;

public class AdminDashboardStatsDto {
    private int totalCustomers;
    private int totalSellers;
    private int pendingSellerApplications;
    private int totalProducts;
    private int totalOrders;
    private double totalSalesRevenue;

    public AdminDashboardStatsDto() {
    }

    public AdminDashboardStatsDto(int totalCustomers, int totalSellers, int pendingSellerApplications, int totalProducts, int totalOrders, double totalSalesRevenue) {
        this.totalCustomers = totalCustomers;
        this.totalSellers = totalSellers;
        this.pendingSellerApplications = pendingSellerApplications;
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalSalesRevenue = totalSalesRevenue;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(int totalSellers) {
        this.totalSellers = totalSellers;
    }

    public int getPendingSellerApplications() {
        return pendingSellerApplications;
    }

    public void setPendingSellerApplications(int pendingSellerApplications) {
        this.pendingSellerApplications = pendingSellerApplications;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalSalesRevenue() {
        return totalSalesRevenue;
    }

    public void setTotalSalesRevenue(double totalSalesRevenue) {
        this.totalSalesRevenue = totalSalesRevenue;
    }
}
