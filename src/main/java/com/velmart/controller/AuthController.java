package com.velmart.controller;

import com.velmart.dto.request.ForgotPasswordRequest;
import com.velmart.dto.request.LoginRequest;
import com.velmart.dto.request.RegisterRequest;
import com.velmart.dto.request.ResetPasswordRequest;
import com.velmart.dto.response.ApiResponse;
import com.velmart.dto.response.UserDto;
import com.velmart.security.UserPrincipal;
import com.velmart.service.AuthService;
import com.velmart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerCustomer(@Valid @RequestBody RegisterRequest request,
                                                                  HttpServletRequest httpRequest) {
        UserDto user = authService.registerCustomer(request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer account registered successfully.", user));
    }

    @PostMapping("/register-seller")
    public ResponseEntity<ApiResponse<UserDto>> registerSeller(@Valid @RequestBody RegisterRequest request,
                                                                HttpServletRequest httpRequest) {
        UserDto user = authService.registerSeller(request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Seller registration submitted successfully. Your account is pending administrator approval.", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        UserDto user = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", user));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                                           HttpServletRequest httpRequest) {
        String token = authService.createPasswordResetToken(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Password reset code generated.", Map.of(
                "message", "A password reset code has been generated.",
                "resetCode", token // In demo/capstone, returning the code simplifies testing
        )));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                           HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please log in with your new password.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated."));
        }
        UserDto userDto = userService.getUserProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Current session active", userDto));
    }
}
