package com.velmart.service;

import com.velmart.dto.response.SellerDashboardStatsDto;
import com.velmart.dto.response.SellerProfileDto;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.model.ApplicationStatus;
import com.velmart.model.FulfillmentStatus;
import com.velmart.model.OrderItem;
import com.velmart.model.Product;
import com.velmart.model.SellerProfile;
import com.velmart.repository.OrderItemRepository;
import com.velmart.repository.ProductRepository;
import com.velmart.repository.SellerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public SellerService(SellerProfileRepository sellerProfileRepository,
                         ProductRepository productRepository,
                         OrderItemRepository orderItemRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public SellerProfileDto getSellerProfileByUserId(String userId) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user ID: " + userId));
        return toDto(profile);
    }

    public SellerProfileDto getSellerProfileById(String sellerId) {
        SellerProfile profile = sellerProfileRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for ID: " + sellerId));
        return toDto(profile);
    }

    public SellerDashboardStatsDto getDashboardStats(String sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        int totalProducts = products.size();
        int activeProducts = (int) products.stream().filter(p -> Boolean.TRUE.equals(p.getActive())).count();
        int outOfStock = (int) products.stream().filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= 0).count();

        List<OrderItem> items = orderItemRepository.findBySellerId(sellerId);
        int totalOrders = (int) items.stream().map(OrderItem::getOrderId).distinct().count();
        int pendingFulfillments = (int) items.stream()
                .filter(i -> i.getFulfillmentStatus() == FulfillmentStatus.PLACED || i.getFulfillmentStatus() == FulfillmentStatus.PROCESSING)
                .count();

        double totalRevenue = items.stream()
                .filter(i -> i.getFulfillmentStatus() != FulfillmentStatus.CANCELLED)
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        return new SellerDashboardStatsDto(
                totalProducts,
                activeProducts,
                outOfStock,
                totalOrders,
                pendingFulfillments,
                Math.round(totalRevenue * 100.0) / 100.0
        );
    }

    public List<SellerProfileDto> getAllSellers() {
        return sellerProfileRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<SellerProfileDto> getPendingSellerApplications() {
        return sellerProfileRepository.findByApplicationStatus(ApplicationStatus.PENDING).stream()
                .map(this::toDto)
                .toList();
    }

    public SellerProfileDto toDto(SellerProfile profile) {
        if (profile == null) return null;
        return new SellerProfileDto(
                profile.getSellerId(),
                profile.getUserId(),
                profile.getBusinessName(),
                profile.getBusinessAddress(),
                profile.getPhone(),
                profile.getApplicationStatus(),
                profile.getApprovalNotes(),
                profile.getCreatedAt()
        );
    }
}
