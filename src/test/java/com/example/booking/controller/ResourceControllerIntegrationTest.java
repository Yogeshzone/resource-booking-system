package com.example.booking.controller;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.LoginResponse;
import com.example.booking.dto.resource.ResourceCreateRequest;
import com.example.booking.dto.resource.ResourceUpdateRequest;
import com.example.booking.entity.Resource;
import com.example.booking.repository.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRepository resourceRepository;

    private String adminToken;
    private String userToken;
    private Resource testResource;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin", "Admin@123");
        userToken = obtainToken("user", "User@123");

        testResource = resourceRepository.save(new Resource(
                "Integration Test Room", "Integration Desc", "ROOM", new BigDecimal("120.00"), true
        ));
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        return response.getToken();
    }

    @Test
    void unauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanListResources_Returns200() throws Exception {
        mockMvc.perform(get("/resources")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void userCanGetResourceById_Returns200() throws Exception {
        mockMvc.perform(get("/resources/" + testResource.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testResource.getId()))
                .andExpect(jsonPath("$.name").value("Integration Test Room"));
    }

    @Test
    void userCannotCreateResource_Returns403Forbidden() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest(
                "Unauthorized Room", "Desc", "ROOM", new BigDecimal("50.00"), true
        );

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotUpdateResource_Returns403Forbidden() throws Exception {
        ResourceUpdateRequest request = new ResourceUpdateRequest("Updated", "Desc", "ROOM", new BigDecimal("99.00"), true);

        mockMvc.perform(put("/resources/" + testResource.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteResource_Returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/resources/" + testResource.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateResource_Returns201Created() throws Exception {
        ResourceCreateRequest request = new ResourceCreateRequest(
                "Admin Created Lab", "High-tech lab", "EQUIPMENT", new BigDecimal("250.00"), true
        );

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Admin Created Lab"))
                .andExpect(jsonPath("$.price").value(250.00));
    }
}
