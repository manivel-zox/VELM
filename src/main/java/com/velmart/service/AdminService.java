package com.velmart.service;

import com.velmart.dto.request.SellerApprovalRequest;
import com.velmart.dto.response.AdminDashboardStatsDto;
import com.velmart.dto.response.SellerProfileDto;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.model.ApplicationStatus;
import com.velmart.model.Order;
import com.velmart.model.OrderStatus;
import com.velmart.model.Role;
import com.velmart.model.SellerProfile;
import com.velmart.model.User;
import com.velmart.repository.OrderRepository;
import com.velmart.repository.ProductRepository;
import com.velmart.repository.SellerProfileRepository;
import com.velmart.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SellerService sellerService;
    private final AuditLogService auditLogService;

    public AdminService(UserRepository userRepository,
                        SellerProfileRepository sellerProfileRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        SellerService sellerService,
                        AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.sellerService = sellerService;
        this.auditLogService = auditLogService;
    }

    public AdminDashboardStatsDto getAdminDashboardStats() {
        List<User> users = userRepository.findAll();
        int totalCustomers = (int) users.stream().filter(u -> u.getRole() == Role.ROLE_CUSTOMER).count();
        int totalSellers = (int) users.stream().filter(u -> u.getRole() == Role.ROLE_SELLER).count();

        List<SellerProfile> sellers = sellerProfileRepository.findAll();
        int pendingApplications = (int) sellers.stream()
                .filter(s -> s.getApplicationStatus() == ApplicationStatus.PENDING)
                .count();

        int totalProducts = productRepository.findAll().size();

        List<Order> orders = orderRepository.findAll();
        int totalOrders = orders.size();
        double totalSalesRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        return new AdminDashboardStatsDto(
                totalCustomers,
                totalSellers,
                pendingApplications,
                totalProducts,
                totalOrders,
                Math.round(totalSalesRevenue * 100.0) / 100.0
        );
    }

    public SellerProfileDto reviewSellerApplication(String adminId, String adminEmail, String sellerId, SellerApprovalRequest request, String clientIp) {
        SellerProfile profile = sellerProfileRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller application not found for ID: " + sellerId));

        profile.setApplicationStatus(request.getStatus());
        profile.setApprovalNotes(request.getNotes() != null ? request.getNotes() : "Reviewed by admin");
        profile.setUpdatedAt(Instant.now().toString());

        sellerProfileRepository.save(profile);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN",
                "REVIEW_SELLER_APPLICATION", "SELLER_PROFILE", sellerId,
                "Application status set to: " + request.getStatus() + ". Notes: " + profile.getApprovalNotes(), clientIp);

        return sellerService.toDto(profile);
    }
}
