package com.example.booking.controller;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.LoginResponse;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.Resource;
import com.example.booking.repository.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationOwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRepository resourceRepository;

    private String user1Token;
    private String user2Token;
    private String adminToken;
    private Resource testResource;

    @BeforeEach
    void setUp() throws Exception {
        user1Token = obtainToken("user", "User@123");
        user2Token = obtainToken("john_doe", "John@123");
        adminToken = obtainToken("admin", "Admin@123");

        testResource = resourceRepository.save(new Resource(
                "Ownership Test Hall", "Exclusive space", "ROOM", new BigDecimal("100.00"), true
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
    void reservationOwnership_EnforcedAtSecurityAndServiceLayer() throws Exception {
        // 1. User 1 creates a reservation for tomorrow 10:00 - 12:00
        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);
        ReservationCreateRequest createRequest = new ReservationCreateRequest(testResource.getId(), start, end);

        MvcResult createResult = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value("user"))
                .andExpect(jsonPath("$.price").value(200.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        ReservationResponse createdReservation = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ReservationResponse.class);
        Long reservationId = createdReservation.getId();

        // 2. User 1 can view their own reservation
        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.user.username").value("user"));

        // 3. User 2 attempts to view User 1's reservation -> 403 Forbidden!
        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        // 4. User 2 attempts to cancel User 1's reservation -> 403 Forbidden!
        mockMvc.perform(patch("/reservations/" + reservationId + "/cancel")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden());

        // 5. Admin can view User 1's reservation
        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId));

        // 6. User 1 successfully cancels their own reservation
        mockMvc.perform(patch("/reservations/" + reservationId + "/cancel")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void userListingReservations_OnlyReturnsOwnReservations() throws Exception {
        // Fetch reservations as User 1
        MvcResult userResult = mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andReturn();

        // Ensure every item returned belongs to "user"
        JsonNode root = objectMapper.readTree(userResult.getResponse().getContentAsString());
        JsonNode items = root.get("content");

        for (int i = 0; i < items.size(); i++) {
            JsonNode res = items.get(i);
            assertEquals("user", res.get("user").get("username").asText());
        }
    }
}
