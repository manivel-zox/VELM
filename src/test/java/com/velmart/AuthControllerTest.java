package com.velmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velmart.dto.request.LoginRequest;
import com.velmart.dto.request.RegisterRequest;
import com.velmart.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should register new customer successfully")
    void testRegisterCustomerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Rahul Kumar");
        request.setEmail("rahul.test" + System.currentTimeMillis() + "@velmart.com");
        request.setPhone("+91 9876543299");
        request.setPassword("Secret@1234");
        request.setRole(Role.ROLE_CUSTOMER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(request.getEmail().toLowerCase()))
                .andExpect(jsonPath("$.data.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should reject customer registration with duplicate email")
    void testDuplicateEmailRegistrationFails() throws Exception {
        String testEmail = "duplicate.test" + System.currentTimeMillis() + "@velmart.com";
        RegisterRequest request = new RegisterRequest();
        request.setName("Rahul Duplicate");
        request.setEmail(testEmail);
        request.setPhone("+91 9876543299");
        request.setPassword("Secret@1234");
        request.setRole(Role.ROLE_CUSTOMER);

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration attempt
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should login existing admin successfully")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest login = new LoginRequest("admin@velmart.com", "Admin@Velmart2026!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testLoginInvalidPasswordFails() throws Exception {
        LoginRequest login = new LoginRequest("admin@velmart.com", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
