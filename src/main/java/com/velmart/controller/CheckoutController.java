package com.velmart.controller;

import com.velmart.dto.request.CheckoutRequest;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.OrderDetailDto;
import com.velmart.security.UserPrincipal;
import com.velmart.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailDto>> placeOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody CheckoutRequest request,
                                                                  HttpServletRequest httpRequest) {
        OrderDetailDto order = orderService.placeOrder(principal.getUserId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully.", order));
    }
}
