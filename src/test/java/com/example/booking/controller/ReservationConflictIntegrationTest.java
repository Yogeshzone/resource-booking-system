package com.example.booking.controller;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.LoginResponse;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.Resource;
import com.example.booking.repository.ResourceRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationConflictIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRepository resourceRepository;

    private String userToken;
    private Resource testResource;

    @BeforeEach
    void setUp() throws Exception {
        userToken = obtainToken("user", "User@123");

        testResource = resourceRepository.save(new Resource(
                "Conflict Testing Facility", "Testing conflicts", "ROOM", new BigDecimal("50.00"), true
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
    void overlappingReservation_IsRejectedWith409Conflict() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now().plusDays(20).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime initialEnd = baseTime.plusHours(2); // 10:00 - 12:00

        ReservationCreateRequest firstBooking = new ReservationCreateRequest(testResource.getId(), baseTime, initialEnd);

        // 1. First booking succeeds
        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstBooking)))
                .andExpect(status().isCreated());

        // 2. Overlapping booking (11:00 - 13:00) fails with 409 Conflict
        ReservationCreateRequest overlappingBooking1 = new ReservationCreateRequest(
                testResource.getId(), baseTime.plusHours(1), baseTime.plusHours(3));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingBooking1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        // 3. Enclosing booking (09:00 - 13:00) fails with 409 Conflict
        ReservationCreateRequest overlappingBooking2 = new ReservationCreateRequest(
                testResource.getId(), baseTime.minusHours(1), baseTime.plusHours(3));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingBooking2)))
                .andExpect(status().isConflict());

        // 4. Back-to-back booking (12:00 - 14:00) SUCCEEDS with [start, end) interval semantics
        ReservationCreateRequest backToBackBooking = new ReservationCreateRequest(
                testResource.getId(), initialEnd, initialEnd.plusHours(2));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backToBackBooking)))
                .andExpect(status().isCreated());
    }

    @Test
    void cancelledReservation_DoesNotBlockNewBooking() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now().plusDays(25).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = baseTime.plusHours(2);

        ReservationCreateRequest booking = new ReservationCreateRequest(testResource.getId(), baseTime, end);

        // Create booking
        MvcResult result = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andReturn();

        ReservationResponse res = objectMapper.readValue(result.getResponse().getContentAsString(), ReservationResponse.class);

        // Cancel it
        mockMvc.perform(patch("/reservations/" + res.getId() + "/cancel")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Re-book the same slot -> SUCCEEDS!
        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
