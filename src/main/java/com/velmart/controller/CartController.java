package com.velmart.controller;

import com.velmart.dto.request.CartAddRequest;
import com.velmart.dto.request.CartUpdateRequest;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.CartViewDto;
import com.velmart.security.UserPrincipal;
import com.velmart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartViewDto>> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartViewDto cart = cartService.getCart(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved.", cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartViewDto>> addToCart(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody CartAddRequest request) {
        CartViewDto cart = cartService.addToCart(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Product added to cart.", cart));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartViewDto>> updateQuantity(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @PathVariable String productId,
                                                                   @Valid @RequestBody CartUpdateRequest request) {
        CartViewDto cart = cartService.updateQuantity(principal.getUserId(), productId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart updated.", cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartViewDto>> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable String productId) {
        CartViewDto cart = cartService.removeItem(principal.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart.", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared.", null));
    }
}
