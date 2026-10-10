package com.velmart.controller;

import com.velmart.dto.request.AddressRequest;
import com.velmart.dto.request.ProfileUpdateRequest;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.OrderDetailDto;
import com.velmart.dto.response.UserDto;
import com.velmart.model.Address;
import com.velmart.security.UserPrincipal;
import com.velmart.service.AddressService;
import com.velmart.service.OrderService;
import com.velmart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;

    public CustomerController(UserService userService,
                              AddressService addressService,
                              OrderService orderService) {
        this.userService = userService;
        this.addressService = addressService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto dto = userService.getUserProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody ProfileUpdateRequest request,
                                                              HttpServletRequest httpRequest) {
        UserDto dto = userService.updateProfile(principal.getUserId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", dto));
    }

    // Address Management
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses(@AuthenticationPrincipal UserPrincipal principal) {
        List<Address> addresses = addressService.getUserAddresses(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Address>> createAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody AddressRequest request) {
        Address created = addressService.createAddress(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Address added successfully.", created));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable String addressId,
                                                              @Valid @RequestBody AddressRequest request) {
        Address updated = addressService.updateAddress(principal.getUserId(), addressId, request);
        return ResponseEntity.ok(ApiResponse.ok("Address updated successfully.", updated));
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable String addressId) {
        addressService.setDefaultAddress(principal.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.ok("Default address updated successfully.", null));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable String addressId) {
        addressService.deleteAddress(principal.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully.", null));
    }

    // Orders Management
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDetailDto>>> getOrders(@AuthenticationPrincipal UserPrincipal principal) {
        List<OrderDetailDto> orders = orderService.getCustomerOrders(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailDto>> getOrderDetails(@AuthenticationPrincipal UserPrincipal principal,
                                                                       @PathVariable String orderId) {
        OrderDetailDto order = orderService.getOrderDetails(principal.getUserId(), orderId, false);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailDto>> cancelOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @PathVariable String orderId,
                                                                   @RequestBody(required = false) Map<String, String> body,
                                                                   HttpServletRequest httpRequest) {
        String reason = body != null ? body.get("reason") : "Cancelled by customer";
        OrderDetailDto cancelled = orderService.cancelOrderByCustomer(principal.getUserId(), orderId, reason, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully.", cancelled));
    }
}
