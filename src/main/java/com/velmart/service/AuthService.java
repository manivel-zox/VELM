package com.velmart.service;

import com.velmart.dto.request.ForgotPasswordRequest;
import com.velmart.dto.request.LoginRequest;
import com.velmart.dto.request.RegisterRequest;
import com.velmart.dto.request.ResetPasswordRequest;
import com.velmart.dto.response.UserDto;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.UnauthorizedActionException;
import com.velmart.model.AccountStatus;
import com.velmart.model.ApplicationStatus;
import com.velmart.model.Role;
import com.velmart.model.SellerProfile;
import com.velmart.model.User;
import com.velmart.repository.SellerProfileRepository;
import com.velmart.repository.UserRepository;
import com.velmart.security.RateLimiterService;
import com.velmart.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RateLimiterService rateLimiterService;
    private final AuditLogService auditLogService;
    private final UserService userService;

    // In-memory store for password reset tokens (token -> email, expiry)
    private final Map<String, ResetTokenInfo> resetTokens = new ConcurrentHashMap<>();

    private static class ResetTokenInfo {
        String email;
        long expiryTime;

        ResetTokenInfo(String email, long expiryTime) {
            this.email = email;
            this.expiryTime = expiryTime;
        }
    }

    public AuthService(UserRepository userRepository,
                       SellerProfileRepository sellerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       RateLimiterService rateLimiterService,
                       AuditLogService auditLogService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.rateLimiterService = rateLimiterService;
        this.auditLogService = auditLogService;
        this.userService = userService;
    }

    public UserDto registerCustomer(RegisterRequest request, String clientIp) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByNormalizedEmail(cleanEmail)) {
            throw new BadRequestException("An account with email " + cleanEmail + " already exists. Please login instead.");
        }

        String userId = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        User user = new User(
                userId,
                request.getName().trim(),
                cleanEmail,
                request.getPhone().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_CUSTOMER,
                AccountStatus.ACTIVE,
                now,
                now
        );

        userRepository.save(user);

        auditLogService.logAction(userId, cleanEmail, Role.ROLE_CUSTOMER.name(),
                "REGISTER_CUSTOMER", "USER", userId, "New customer account created", clientIp);

        return userService.toDto(user);
    }

    public UserDto registerSeller(RegisterRequest request, String clientIp) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByNormalizedEmail(cleanEmail)) {
            throw new BadRequestException("An account with email " + cleanEmail + " already exists.");
        }

        if (request.getBusinessName() == null || request.getBusinessName().isBlank()) {
            throw new BadRequestException("Business name is required for seller registration.");
        }

        String userId = UUID.randomUUID().toString();
        String sellerId = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        User user = new User(
                userId,
                request.getName().trim(),
                cleanEmail,
                request.getPhone().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_SELLER,
                AccountStatus.ACTIVE,
                now,
                now
        );

        userRepository.save(user);

        SellerProfile sellerProfile = new SellerProfile(
                sellerId,
                userId,
                request.getBusinessName().trim(),
                request.getBusinessAddress() != null ? request.getBusinessAddress().trim() : "",
                request.getPhone().trim(),
                ApplicationStatus.PENDING,
                "Application pending administrator review",
                now,
                now
        );

        sellerProfileRepository.save(sellerProfile);

        auditLogService.logAction(userId, cleanEmail, Role.ROLE_SELLER.name(),
                "REGISTER_SELLER", "SELLER_PROFILE", sellerId, "New seller application submitted", clientIp);

        return userService.toDto(user);
    }

    public UserDto login(LoginRequest request, HttpServletRequest httpRequest) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        String clientIp = httpRequest.getRemoteAddr();

        if (rateLimiterService.isBlocked(cleanEmail)) {
            throw new UnauthorizedActionException("Too many failed login attempts. Please try again after 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            rateLimiterService.loginSucceeded(cleanEmail);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getUserId()).orElseThrow();

            auditLogService.logAction(user.getUserId(), user.getNormalizedEmail(), user.getRole().name(),
                    "LOGIN_SUCCESS", "USER", user.getUserId(), "User logged in successfully", clientIp);

            return userService.toDto(user);

        } catch (BadCredentialsException ex) {
            rateLimiterService.loginFailed(cleanEmail);
            auditLogService.logAction(null, cleanEmail, "UNKNOWN",
                    "LOGIN_FAILED", "USER", null, "Invalid password attempt", clientIp);
            throw new BadRequestException("Invalid email or password.");
        } catch (LockedException ex) {
            throw new UnauthorizedActionException(ex.getMessage());
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public String createPasswordResetToken(ForgotPasswordRequest request, String clientIp) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByNormalizedEmail(cleanEmail)
                .orElseThrow(() -> new BadRequestException("No account found with email: " + cleanEmail));

        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long expiry = System.currentTimeMillis() + (30 * 60 * 1000); // 30 minutes
        resetTokens.put(token, new ResetTokenInfo(cleanEmail, expiry));

        auditLogService.logAction(user.getUserId(), cleanEmail, user.getRole().name(),
                "FORGOT_PASSWORD_REQUEST", "USER", user.getUserId(), "Generated reset code: " + token, clientIp);

        // In a live system, this sends an email. For demo/capstone, token is returned or logged.
        return token;
    }

    public void resetPassword(ResetPasswordRequest request, String clientIp) {
        String token = request.getToken().trim();
        ResetTokenInfo info = resetTokens.get(token);

        if (info == null || System.currentTimeMillis() > info.expiryTime) {
            resetTokens.remove(token);
            throw new BadRequestException("Invalid or expired password reset token.");
        }

        if (!info.email.equalsIgnoreCase(request.getEmail().trim())) {
            throw new BadRequestException("Reset token does not match provided email.");
        }

        User user = userRepository.findByNormalizedEmail(info.email)
                .orElseThrow(() -> new BadRequestException("User not found."));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now().toString());
        userRepository.save(user);

        resetTokens.remove(token);

        auditLogService.logAction(user.getUserId(), user.getNormalizedEmail(), user.getRole().name(),
                "RESET_PASSWORD", "USER", user.getUserId(), "Password successfully reset via token", clientIp);
    }
}
