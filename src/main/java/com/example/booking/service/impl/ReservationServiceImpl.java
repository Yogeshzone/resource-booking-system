package com.example.booking.service.impl;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.reservation.AdminReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.dto.reservation.ReservationUpdateRequest;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.Resource;
import com.example.booking.entity.User;
import com.example.booking.enums.ReservationStatus;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.InvalidStatusTransitionException;
import com.example.booking.exception.ReservationConflictException;
import com.example.booking.exception.ReservationNotFoundException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.mapper.ReservationMapper;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.SecurityUtils;
import com.example.booking.security.UserPrincipal;
import com.example.booking.service.ReservationService;
import com.example.booking.specification.ReservationSpecification;
import com.example.booking.util.PriceCalculator;
import com.example.booking.util.SortUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private static final List<ReservationStatus> BLOCKING_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        log.info("User {} creating reservation for resource {}", authenticatedUserId, request.getResourceId());

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found in database"));

        return processReservationCreation(
                currentUser,
                request.getResourceId(),
                request.getStartTime(),
                request.getEndTime(),
                ReservationStatus.PENDING
        );
    }

    @Override
    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationCreateRequest request) {
        User targetUser;
        if (request.getUserId() != null) {
            targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BadRequestException("Target user not found with ID: " + request.getUserId()));
        } else {
            Long currentAdminId = SecurityUtils.getCurrentUserId();
            targetUser = userRepository.findById(currentAdminId)
                    .orElseThrow(() -> new BadRequestException("Current admin user not found in database"));
        }

        ReservationStatus status = request.getStatus() != null ? request.getStatus() : ReservationStatus.PENDING;
        log.info("Admin creating reservation for user {} on resource {}", targetUser.getId(), request.getResourceId());

        return processReservationCreation(
                targetUser,
                request.getResourceId(),
                request.getStartTime(),
                request.getEndTime(),
                status
        );
    }

    private ReservationResponse processReservationCreation(
            User user, Long resourceId, LocalDateTime startTime, LocalDateTime endTime, ReservationStatus status) {

        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be strictly after start time");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource '" + resource.getName() + "' is currently unavailable for booking");
        }

        // Conflict check: Active reservations (PENDING or CONFIRMED) cannot overlap [start, end)
        boolean hasConflict = reservationRepository.existsConflictingReservation(
                resourceId, startTime, endTime, BLOCKING_STATUSES, null
        );

        if (hasConflict) {
            throw new ReservationConflictException(String.format(
                    "Resource '%s' (ID: %d) is already booked during the requested interval %s to %s",
                    resource.getName(), resourceId, startTime, endTime
            ));
        }

        // Calculate price server-side
        BigDecimal price = PriceCalculator.calculatePrice(resource.getPrice(), startTime, endTime);

        Reservation reservation = new Reservation(
                resource,
                user,
                startTime,
                endTime,
                price,
                status
        );

        Reservation saved = reservationRepository.save(reservation);
        log.info("Successfully created reservation ID {} with price {} and status {}", saved.getId(), price, status);

        return reservationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        SecurityUtils.validateOwnershipOrAdmin(reservation.getUser().getId());
        return reservationMapper.toResponseDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long resourceId,
            Long userId,
            Integer page,
            Integer size,
            String sort) {

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        Long effectiveUserId;

        if (currentUser.isAdmin()) {
            effectiveUserId = userId; // Admin can view all or filter by specific user
        } else {
            effectiveUserId = currentUser.getId(); // Normal user ONLY sees their own reservations
        }

        Pageable pageable = SortUtils.createPageable(
                page,
                size,
                sort,
                SortUtils.ALLOWED_RESERVATION_SORT_FIELDS,
                "createdAt",
                Sort.Direction.DESC
        );

        Specification<Reservation> spec = ReservationSpecification.withFilters(
                status, minPrice, maxPrice, resourceId, effectiveUserId
        );

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);

        List<ReservationResponse> responses = reservationPage.getContent().stream()
                .map(reservationMapper::toResponseDto)
                .toList();

        return new PagedResponse<>(
                responses,
                reservationPage.getNumber(),
                reservationPage.getSize(),
                reservationPage.getTotalElements(),
                reservationPage.getTotalPages(),
                reservationPage.isFirst(),
                reservationPage.isLast()
        );
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        SecurityUtils.validateOwnershipOrAdmin(reservation.getUser().getId());

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation ID {} has been cancelled by user ID {}", id, SecurityUtils.getCurrentUserId());

        return reservationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        boolean timesChanged = false;
        LocalDateTime newStart = reservation.getStartTime();
        LocalDateTime newEnd = reservation.getEndTime();

        if (request.getStartTime() != null || request.getEndTime() != null) {
            newStart = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
            newEnd = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();

            if (!newEnd.isAfter(newStart)) {
                throw new BadRequestException("End time must be strictly after start time");
            }
            timesChanged = true;
        }

        if (request.getStatus() != null) {
            validateStatusTransition(reservation.getStatus(), request.getStatus());
            reservation.setStatus(request.getStatus());
        }

        if (timesChanged) {
            boolean hasConflict = reservationRepository.existsConflictingReservation(
                    reservation.getResource().getId(), newStart, newEnd, BLOCKING_STATUSES, reservation.getId()
            );

            if (hasConflict) {
                throw new ReservationConflictException(String.format(
                        "Resource '%s' (ID: %d) is already booked during %s to %s",
                        reservation.getResource().getName(), reservation.getResource().getId(), newStart, newEnd
                ));
            }

            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            reservation.setPrice(PriceCalculator.calculatePrice(reservation.getResource().getPrice(), newStart, newEnd));
        }

        Reservation saved = reservationRepository.save(reservation);
        log.info("Admin updated reservation ID {}", id);
        return reservationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservationStatus(Long id, ReservationStatus targetStatus) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        validateStatusTransition(reservation.getStatus(), targetStatus);
        reservation.setStatus(targetStatus);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Updated reservation ID {} status to {}", id, targetStatus);
        return reservationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservationRepository.delete(reservation);
        log.info("Admin deleted reservation ID {}", id);
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }

        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from CANCELLED to " + targetStatus + ". Cancelled reservations are terminal."
            );
        }

        // PENDING -> CONFIRMED or CANCELLED is allowed
        if (currentStatus == ReservationStatus.PENDING &&
                (targetStatus == ReservationStatus.CONFIRMED || targetStatus == ReservationStatus.CANCELLED)) {
            return;
        }

        // CONFIRMED -> CANCELLED is allowed (or back to PENDING if admin requests)
        if (currentStatus == ReservationStatus.CONFIRMED &&
                (targetStatus == ReservationStatus.CANCELLED || targetStatus == ReservationStatus.PENDING)) {
            return;
        }

        throw new InvalidStatusTransitionException(
                "Invalid reservation status transition from " + currentStatus + " to " + targetStatus
        );
    }
}
