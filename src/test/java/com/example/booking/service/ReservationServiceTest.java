package com.example.booking.service;

import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.Resource;
import com.example.booking.entity.User;
import com.example.booking.enums.ReservationStatus;
import com.example.booking.enums.Role;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.InvalidStatusTransitionException;
import com.example.booking.exception.ReservationConflictException;
import com.example.booking.mapper.ReservationMapper;
import com.example.booking.mapper.ResourceMapper;
import com.example.booking.mapper.UserMapper;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.UserPrincipal;
import com.example.booking.service.impl.ReservationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    private ReservationServiceImpl reservationService;

    private User testUser;
    private Resource testResource;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        ResourceMapper resourceMapper = new ResourceMapper();
        UserMapper userMapper = new UserMapper();
        ReservationMapper reservationMapper = new ReservationMapper(resourceMapper, userMapper);

        reservationService = new ReservationServiceImpl(
                reservationRepository,
                resourceRepository,
                userRepository,
                reservationMapper
        );

        testUser = new User("user", "user@example.com", "pass", Role.USER);
        testUser.setId(1L);

        testResource = new Resource("Test Room", "Desc", "ROOM", new BigDecimal("100.00"), true);
        testResource.setId(1L);

        userPrincipal = new UserPrincipal(
                1L, "user", "user@example.com", "pass", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReservation_Success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        ReservationCreateRequest request = new ReservationCreateRequest(1L, start, end);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(reservationRepository.existsConflictingReservation(eq(1L), eq(start), eq(end), anyList(), eq(null))).thenReturn(false);

        Reservation savedReservation = new Reservation(testResource, testUser, start, end, new BigDecimal("200.00"), ReservationStatus.PENDING);
        savedReservation.setId(10L);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getPrice());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_EndTimeBeforeStartTime_ThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        LocalDateTime end = start.minusHours(2);
        ReservationCreateRequest request = new ReservationCreateRequest(1L, start, end);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void createReservation_UnavailableResource_ThrowsBadRequestException() {
        testResource.setAvailable(false);
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        ReservationCreateRequest request = new ReservationCreateRequest(1L, start, end);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void createReservation_ConflictExists_ThrowsReservationConflictException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        ReservationCreateRequest request = new ReservationCreateRequest(1L, start, end);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(reservationRepository.existsConflictingReservation(eq(1L), eq(start), eq(end), anyList(), eq(null))).thenReturn(true);

        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void cancelReservation_AlreadyCancelled_ThrowsInvalidStatusTransitionException() {
        Reservation reservation = new Reservation(testResource, testUser, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                new BigDecimal("100.00"), ReservationStatus.CANCELLED);
        reservation.setId(5L);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidStatusTransitionException.class, () -> reservationService.cancelReservation(5L));
    }

    @Test
    void getAllReservations_WithFilter_ReturnsPagedResponse() {
        com.example.booking.dto.reservation.ReservationFilterRequest filter =
                new com.example.booking.dto.reservation.ReservationFilterRequest();
        filter.setStatus(ReservationStatus.PENDING);

        org.springframework.data.domain.Page<Reservation> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(reservationRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        com.example.booking.dto.common.PagedResponse<ReservationResponse> response =
                reservationService.getAllReservations(filter);

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void updateReservation_TimesChanged_RecalculatesPriceAndSucceeds() {
        LocalDateTime originalStart = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime originalEnd = originalStart.plusHours(2);
        Reservation reservation = new Reservation(testResource, testUser, originalStart, originalEnd, new BigDecimal("200.00"), ReservationStatus.PENDING);
        reservation.setId(20L);

        when(reservationRepository.findById(20L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsConflictingReservation(eq(1L), any(), any(), anyList(), eq(20L))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime newStart = originalStart.plusHours(1);
        LocalDateTime newEnd = originalEnd.plusHours(3);
        com.example.booking.dto.reservation.ReservationUpdateRequest request =
                new com.example.booking.dto.reservation.ReservationUpdateRequest(newStart, newEnd, ReservationStatus.CONFIRMED);

        ReservationResponse response = reservationService.updateReservation(20L, request);
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("400.00"), response.getPrice());
    }

    @Test
    void updateReservation_Conflict_ThrowsReservationConflictException() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        Reservation reservation = new Reservation(testResource, testUser, start, end, new BigDecimal("200.00"), ReservationStatus.PENDING);
        reservation.setId(21L);

        when(reservationRepository.findById(21L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsConflictingReservation(eq(1L), any(), any(), anyList(), eq(21L))).thenReturn(true);

        com.example.booking.dto.reservation.ReservationUpdateRequest request =
                new com.example.booking.dto.reservation.ReservationUpdateRequest(start.plusHours(1), end.plusHours(1), null);

        assertThrows(ReservationConflictException.class, () -> reservationService.updateReservation(21L, request));
    }

    @Test
    void updateReservation_InvalidTransition_ThrowsInvalidStatusTransitionException() {
        Reservation cancelledReservation = new Reservation(testResource, testUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"), ReservationStatus.CANCELLED);
        cancelledReservation.setId(22L);

        when(reservationRepository.findById(22L)).thenReturn(Optional.of(cancelledReservation));

        com.example.booking.dto.reservation.ReservationUpdateRequest request =
                new com.example.booking.dto.reservation.ReservationUpdateRequest(null, null, ReservationStatus.CONFIRMED);

        assertThrows(InvalidStatusTransitionException.class, () -> reservationService.updateReservation(22L, request));
    }

    @Test
    void updateReservationStatus_Success() {
        Reservation reservation = new Reservation(testResource, testUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"), ReservationStatus.PENDING);
        reservation.setId(23L);

        when(reservationRepository.findById(23L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsConflictingReservation(eq(1L), any(), any(), anyList(), eq(23L))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.updateReservationStatus(23L, ReservationStatus.CONFIRMED);
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void deleteReservation_Success() {
        Reservation reservation = new Reservation(testResource, testUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"), ReservationStatus.PENDING);
        reservation.setId(24L);

        when(reservationRepository.findById(24L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(24L);
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void createAdminReservation_ExplicitUser_Success() {
        User adminUser = new User("admin", "admin@example.com", "pass", Role.ADMIN);
        adminUser.setId(99L);
        User targetUser = new User("target", "target@example.com", "pass", Role.USER);
        targetUser.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(reservationRepository.existsConflictingReservation(eq(1L), any(), any(), anyList(), eq(null))).thenReturn(false);

        Reservation savedReservation = new Reservation(testResource, targetUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("200.00"), ReservationStatus.CONFIRMED);
        savedReservation.setId(25L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        com.example.booking.dto.reservation.AdminReservationCreateRequest request =
                new com.example.booking.dto.reservation.AdminReservationCreateRequest(
                        2L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), ReservationStatus.CONFIRMED
                );

        ReservationResponse response = reservationService.createAdminReservation(request);
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void getReservationById_Success() {
        Reservation reservation = new Reservation(testResource, testUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"), ReservationStatus.PENDING);
        reservation.setId(26L);

        when(reservationRepository.findById(26L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(26L);
        assertNotNull(response);
        assertEquals(26L, response.getId());
    }
}
