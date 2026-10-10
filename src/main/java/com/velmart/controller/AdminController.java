package com.velmart.controller;

import com.velmart.dto.request.CategoryRequest;
import com.velmart.dto.request.OrderStatusUpdateRequest;
import com.velmart.dto.request.SellerApprovalRequest;
import com.velmart.dto.request.UserStatusRequest;
import com.velmart.dto.response.AdminDashboardStatsDto;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.OrderDetailDto;
import com.velmart.dto.response.SellerProfileDto;
import com.velmart.dto.response.UserDto;
import com.velmart.model.AuditLog;
import com.velmart.model.Category;
import com.velmart.model.Product;
import com.velmart.security.UserPrincipal;
import com.velmart.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final SellerService sellerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService,
                           UserService userService,
                           SellerService sellerService,
                           CategoryService categoryService,
                           ProductService productService,
                           OrderService orderService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.userService = userService;
        this.sellerService = sellerService;
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getStats() {
        AdminDashboardStatsDto stats = adminService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // User Governance
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable String userId,
                                                              @Valid @RequestBody UserStatusRequest request,
                                                              HttpServletRequest httpRequest) {
        userService.updateAccountStatus(principal.getUserId(), principal.getEmail(), userId, request.getStatus(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully to " + request.getStatus(), null));
    }

    // Seller Approval Management
    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerProfileDto>>> getAllSellers() {
        List<SellerProfileDto> sellers = sellerService.getAllSellers();
        return ResponseEntity.ok(ApiResponse.ok(sellers));
    }

    @GetMapping("/sellers/pending")
    public ResponseEntity<ApiResponse<List<SellerProfileDto>>> getPendingSellers() {
        List<SellerProfileDto> sellers = sellerService.getPendingSellerApplications();
        return ResponseEntity.ok(ApiResponse.ok(sellers));
    }

    @PostMapping("/sellers/{sellerId}/review")
    public ResponseEntity<ApiResponse<SellerProfileDto>> reviewSeller(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @PathVariable String sellerId,
                                                                      @Valid @RequestBody SellerApprovalRequest request,
                                                                      HttpServletRequest httpRequest) {
        SellerProfileDto reviewed = adminService.reviewSellerApplication(principal.getUserId(), principal.getEmail(), sellerId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Seller application " + request.getStatus(), reviewed));
    }

    // Category Management
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@AuthenticationPrincipal UserPrincipal principal,
                                                                @Valid @RequestBody CategoryRequest request,
                                                                HttpServletRequest httpRequest) {
        Category category = categoryService.createCategory(request, principal.getUserId(), principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created successfully.", category));
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable String categoryId,
                                                                @Valid @RequestBody CategoryRequest request,
                                                                HttpServletRequest httpRequest) {
        Category category = categoryService.updateCategory(categoryId, request, principal.getUserId(), principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully.", category));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable String categoryId,
                                                            HttpServletRequest httpRequest) {
        categoryService.deleteCategory(categoryId, principal.getUserId(), principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully.", null));
    }

    // Product Oversight
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProductsForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @PatchMapping("/products/{productId}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleProductActive(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable String productId,
                                                                 @RequestBody Map<String, Boolean> body,
                                                                 HttpServletRequest httpRequest) {
        boolean active = body.getOrDefault("active", true);
        productService.adminToggleProductActive(principal.getUserId(), principal.getEmail(), productId, active, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Product visibility updated.", null));
    }

    // Order Oversight
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDetailDto>>> getAllOrders() {
        List<OrderDetailDto> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDetailDto>> updateOrderStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                                         @PathVariable String orderId,
                                                                         @Valid @RequestBody OrderStatusUpdateRequest request,
                                                                         HttpServletRequest httpRequest) {
        OrderDetailDto order = orderService.updateOrderStatusByAdmin(principal.getUserId(), principal.getEmail(), orderId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated.", order));
    }

    // Audit Logs
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = auditLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
