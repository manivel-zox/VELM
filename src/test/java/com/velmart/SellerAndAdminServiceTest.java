package com.velmart;

import com.velmart.dto.request.RegisterRequest;
import com.velmart.dto.request.SellerApprovalRequest;
import com.velmart.dto.response.AdminDashboardStatsDto;
import com.velmart.dto.response.SellerDashboardStatsDto;
import com.velmart.dto.response.SellerProfileDto;
import com.velmart.dto.response.UserDto;
import com.velmart.model.ApplicationStatus;
import com.velmart.model.Role;
import com.velmart.service.AdminService;
import com.velmart.service.AuthService;
import com.velmart.service.SellerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SellerAndAdminServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private AdminService adminService;

    @Test
    @DisplayName("Seller onboarding and Administrator approval workflow")
    void testSellerOnboardingAndApprovalWorkflow() {
        String testEmail = "vendor.test" + System.currentTimeMillis() + "@velmart.com";

        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("Sunil Mehta");
        regReq.setEmail(testEmail);
        regReq.setPhone("+91 9123456780");
        regReq.setPassword("Vendor@1234");
        regReq.setRole(Role.ROLE_SELLER);
        regReq.setBusinessName("Mehta Timepieces & Horology");
        regReq.setBusinessAddress("Sector 18, Noida, UP 201301");

        UserDto sellerUser = authService.registerSeller(regReq, "127.0.0.1");
        assertNotNull(sellerUser);
        assertEquals(Role.ROLE_SELLER, sellerUser.getRole());

        // Verify seller profile is initially PENDING
        SellerProfileDto profile = sellerService.getSellerProfileByUserId(sellerUser.getUserId());
        assertNotNull(profile);
        assertEquals(ApplicationStatus.PENDING, profile.getApplicationStatus());

        // Admin reviews and approves the application
        SellerApprovalRequest approvalReq = new SellerApprovalRequest();
        approvalReq.setStatus(ApplicationStatus.APPROVED);
        approvalReq.setNotes("Verified GST and business registration documents.");

        SellerProfileDto approved = adminService.reviewSellerApplication(
                "admin-01", "admin@velmart.com", profile.getSellerId(), approvalReq, "127.0.0.1"
        );

        assertNotNull(approved);
        assertEquals(ApplicationStatus.APPROVED, approved.getApplicationStatus());

        // Verify seller profile reflects APPROVED
        SellerProfileDto verifiedProfile = sellerService.getSellerProfileById(profile.getSellerId());
        assertEquals(ApplicationStatus.APPROVED, verifiedProfile.getApplicationStatus());
    }

    @Test
    @DisplayName("Admin dashboard statistics return accurate platform metrics")
    void testAdminDashboardStats() {
        AdminDashboardStatsDto stats = adminService.getAdminDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalProducts() >= 8, "Should reflect seeded products");
        assertTrue(stats.getTotalCustomers() >= 1, "Should reflect seeded customers");
        assertTrue(stats.getTotalSellers() >= 1, "Should reflect seeded sellers");
    }

    @Test
    @DisplayName("Seller dashboard statistics return accurate seller metrics")
    void testSellerDashboardStats() {
        // Default seeded seller
        String defaultSellerId = "seller-velmart-watches-001";
        SellerDashboardStatsDto stats = sellerService.getDashboardStats(defaultSellerId);

        assertNotNull(stats);
        assertTrue(stats.getTotalProducts() >= 8, "Should have seeded watches and items");
        assertTrue(stats.getActiveProducts() >= 8);
    }
}
