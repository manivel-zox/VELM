package com.velmart.service;

import com.velmart.dto.request.ProfileUpdateRequest;
import com.velmart.dto.response.UserDto;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.model.AccountStatus;
import com.velmart.model.User;
import com.velmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public UserDto getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return toDto(user);
    }

    public UserDto updateProfile(String userId, ProfileUpdateRequest request, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setName(request.getName().trim());
        user.setPhone(request.getPhone().trim());

        // Password change if requested
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to set a new password.");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Incorrect current password.");
            }
            if (request.getNewPassword().length() < 6) {
                throw new BadRequestException("New password must be at least 6 characters.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            auditLogService.logAction(user.getUserId(), user.getNormalizedEmail(), user.getRole().name(),
                    "CHANGE_PASSWORD", "USER", user.getUserId(), "User changed password", clientIp);
        }

        user.setUpdatedAt(Instant.now().toString());
        userRepository.save(user);

        return toDto(user);
    }

    public void updateAccountStatus(String adminId, String adminEmail, String targetUserId, AccountStatus newStatus, String clientIp) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));
        target.setAccountStatus(newStatus);
        target.setUpdatedAt(Instant.now().toString());
        userRepository.save(target);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN", "UPDATE_USER_STATUS",
                "USER", targetUserId, "Status updated to " + newStatus, clientIp);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getUserId(),
                user.getName(),
                user.getNormalizedEmail(),
                user.getPhone(),
                user.getRole(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }
}
