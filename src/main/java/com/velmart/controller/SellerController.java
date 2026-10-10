package com.velmart.controller;

import com.velmart.dto.request.FulfillmentStatusRequest;
import com.velmart.dto.request.ProductRequest;
import com.velmart.dto.request.ProductStatusRequest;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.SellerDashboardStatsDto;
import com.velmart.dto.response.SellerProfileDto;
import com.velmart.exception.BadRequestException;
import com.velmart.model.ApplicationStatus;
import com.velmart.model.OrderItem;
import com.velmart.model.Product;
import com.velmart.security.UserPrincipal;
import com.velmart.service.OrderService;
import com.velmart.service.ProductService;
import com.velmart.service.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;
    private final ProductService productService;
    private final OrderService orderService;

    public SellerController(SellerService sellerService,
                            ProductService productService,
                            OrderService orderService) {
        this.sellerService = sellerService;
        this.productService = productService;
        this.orderService = orderService;
    }

    private SellerProfileDto getApprovedSeller(UserPrincipal principal) {
        SellerProfileDto profile = sellerService.getSellerProfileByUserId(principal.getUserId());
        if (profile.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Your seller account is not yet approved. Status: " + profile.getApplicationStatus());
        }
        return profile;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileDto>> getSellerProfile(@AuthenticationPrincipal UserPrincipal principal) {
        SellerProfileDto profile = sellerService.getSellerProfileByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SellerDashboardStatsDto>> getDashboardStats(@AuthenticationPrincipal UserPrincipal principal) {
        SellerProfileDto profile = getApprovedSeller(principal);
        SellerDashboardStatsDto stats = sellerService.getDashboardStats(profile.getSellerId());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getMyProducts(@AuthenticationPrincipal UserPrincipal principal) {
        SellerProfileDto profile = getApprovedSeller(principal);
        List<Product> products = productService.getSellerProducts(profile.getSellerId());
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> addProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody ProductRequest request,
                                                           HttpServletRequest httpRequest) {
        SellerProfileDto profile = getApprovedSeller(principal);
        Product product = productService.createProduct(profile.getSellerId(), request, principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product added successfully.", product));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable String productId,
                                                              @Valid @RequestBody ProductRequest request,
                                                              HttpServletRequest httpRequest) {
        SellerProfileDto profile = getApprovedSeller(principal);
        Product updated = productService.updateProduct(profile.getSellerId(), productId, request, principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully.", updated));
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<ApiResponse<Product>> updateProductStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @PathVariable String productId,
                                                                    @RequestBody ProductStatusRequest request,
                                                                    HttpServletRequest httpRequest) {
        SellerProfileDto profile = getApprovedSeller(principal);
        Product updated = productService.updateProductStatus(profile.getSellerId(), productId, request, principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Product status updated.", updated));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable String productId,
                                                           HttpServletRequest httpRequest) {
        SellerProfileDto profile = getApprovedSeller(principal);
        productService.deleteProductBySeller(profile.getSellerId(), productId, principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully.", null));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        SellerProfileDto profile = getApprovedSeller(principal);
        List<OrderItem> items = orderService.getSellerOrderItems(profile.getSellerId());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PatchMapping("/orders/items/{orderItemId}/fulfillment")
    public ResponseEntity<ApiResponse<OrderItem>> updateFulfillment(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @PathVariable String orderItemId,
                                                                    @Valid @RequestBody FulfillmentStatusRequest request,
                                                                    HttpServletRequest httpRequest) {
        SellerProfileDto profile = getApprovedSeller(principal);
        OrderItem updated = orderService.updateItemFulfillmentStatus(profile.getSellerId(), orderItemId, request, principal.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Fulfillment status updated.", updated));
    }
}
