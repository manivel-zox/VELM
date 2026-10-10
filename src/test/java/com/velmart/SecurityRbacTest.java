package com.velmart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoints should be accessible without authentication")
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthenticated request to Admin endpoints should return 401")
    void testUnauthenticatedAdminAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@velmart.com", authorities = {"ROLE_CUSTOMER"})
    @DisplayName("Customer cannot access Admin endpoints (403 Forbidden)")
    void testCustomerCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@velmart.com", authorities = {"ROLE_CUSTOMER"})
    @DisplayName("Customer cannot access Seller endpoints (403 Forbidden)")
    void testCustomerCannotAccessSeller() throws Exception {
        mockMvc.perform(get("/api/seller/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@velmart.com", authorities = {"ROLE_ADMIN"})
    @DisplayName("Admin can access Admin stats successfully")
    void testAdminAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
