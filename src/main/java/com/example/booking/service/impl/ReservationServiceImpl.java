package com.example.booking.service.impl;

import com.example.booking.dto.common.PagedResponse;
import com.example.booking.dto.reservation.AdminReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationCreateRequest;
import com.example.booking.dto.reservation.ReservationFilterRequest;
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
import com.example.booking.exception.UserNotFoundException;
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
        log.info("Creating reservation for resource {}", request.getResourceId());

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException(authenticatedUserId));

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
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        } else {
            Long currentAdminId = SecurityUtils.getCurrentUserId();
            targetUser = userRepository.findById(currentAdminId)
                    .orElseThrow(() -> new UserNotFoundException(currentAdminId));
        }

        ReservationStatus status = request.getStatus() != null ? request.getStatus() : ReservationStatus.PENDING;
        log.info("Admin creating reservation on resource {}", request.getResourceId());

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

        validateReservationTimes(startTime, endTime);

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        validateResourceAvailability(resource);
        checkReservationConflict(resourceId, startTime, endTime, null, resource.getName());

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
    public PagedResponse<ReservationResponse> getAllReservations(ReservationFilterRequest filter) {
        ReservationFilterRequest request = filter != null ? filter : new ReservationFilterRequest();

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        Long effectiveUserId = currentUser.isAdmin() ? request.getUserId() : currentUser.getId();

        Pageable pageable = SortUtils.createPageable(
                request.getPage(),
                request.getSize(),
                request.getSort(),
                SortUtils.ALLOWED_RESERVATION_SORT_FIELDS,
                "createdAt",
                Sort.Direction.DESC
        );

        Specification<Reservation> spec = ReservationSpecification.withFilters(
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getResourceId(),
                effectiveUserId
        );

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);
        List<ReservationResponse> responses = reservationPage.getContent().stream()
                .map(reservationMapper::toResponseDto)
                .toList();

        return PagedResponse.<ReservationResponse>builder()
                .content(responses)
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .first(reservationPage.isFirst())
                .last(reservationPage.isLast())
                .build();
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
        log.info("Reservation ID {} has been cancelled", id);

        return reservationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        LocalDateTime newStart = resolveUpdatedStartTime(reservation, request);
        LocalDateTime newEnd = resolveUpdatedEndTime(reservation, request);
        boolean timesChanged = hasTimesChanged(reservation, newStart, newEnd);

        if (request.getStartTime() != null || request.getEndTime() != null) {
            validateReservationTimes(newStart, newEnd);
        }

        ReservationStatus newStatus = resolveUpdatedStatus(reservation, request);
        boolean statusChanged = (newStatus != reservation.getStatus());

        if (shouldCheckForConflicts(timesChanged, statusChanged, newStatus)) {
            checkReservationConflict(
                    reservation.getResource().getId(),
                    newStart,
                    newEnd,
                    reservation.getId(),
                    reservation.getResource().getName()
            );
        }

        applyReservationUpdates(reservation, newStart, newEnd, newStatus, timesChanged);
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

        if (targetStatus.isBlocking()) {
            checkReservationConflict(
                    reservation.getResource().getId(),
                    reservation.getStartTime(),
                    reservation.getEndTime(),
                    reservation.getId(),
                    reservation.getResource().getName()
            );
        }

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

    private void validateReservationTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be strictly after start time");
        }
    }

    private void validateResourceAvailability(Resource resource) {
        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource '" + resource.getName() + "' is currently unavailable for booking");
        }
    }

    private void checkReservationConflict(
            Long resourceId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId, String resourceName) {
        boolean hasConflict = reservationRepository.existsConflictingReservation(
                resourceId, startTime, endTime, ReservationStatus.BLOCKING_STATUSES, excludeReservationId
        );

        if (hasConflict) {
            throw new ReservationConflictException(String.format(
                    "Resource '%s' (ID: %d) is already booked during the requested interval %s to %s",
                    resourceName, resourceId, startTime, endTime
            ));
        }
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

        // CONFIRMED -> CANCELLED or PENDING is allowed
        if (currentStatus == ReservationStatus.CONFIRMED &&
                (targetStatus == ReservationStatus.CANCELLED || targetStatus == ReservationStatus.PENDING)) {
            return;
        }

        throw new InvalidStatusTransitionException(
                "Invalid reservation status transition from " + currentStatus + " to " + targetStatus
        );
    }

    private LocalDateTime resolveUpdatedStartTime(Reservation reservation, ReservationUpdateRequest request) {
        return (request != null && request.getStartTime() != null) ? request.getStartTime() : reservation.getStartTime();
    }

    private LocalDateTime resolveUpdatedEndTime(Reservation reservation, ReservationUpdateRequest request) {
        return (request != null && request.getEndTime() != null) ? request.getEndTime() : reservation.getEndTime();
    }

    private boolean hasTimesChanged(Reservation reservation, LocalDateTime newStart, LocalDateTime newEnd) {
        return !newStart.equals(reservation.getStartTime()) || !newEnd.equals(reservation.getEndTime());
    }

    private ReservationStatus resolveUpdatedStatus(Reservation reservation, ReservationUpdateRequest request) {
        if (request != null && request.getStatus() != null && request.getStatus() != reservation.getStatus()) {
            validateStatusTransition(reservation.getStatus(), request.getStatus());
            return request.getStatus();
        }
        return reservation.getStatus();
    }

    private void applyReservationUpdates(
            Reservation reservation,
            LocalDateTime newStart,
            LocalDateTime newEnd,
            ReservationStatus newStatus,
            boolean timesChanged) {
        if (timesChanged) {
            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            reservation.setPrice(PriceCalculator.calculatePrice(reservation.getResource().getPrice(), newStart, newEnd));
        }
        reservation.setStatus(newStatus);
    }

    private boolean shouldCheckForConflicts(boolean timesChanged, boolean statusChanged, ReservationStatus newStatus) {
        return timesChanged || (statusChanged && newStatus.isBlocking());
    }
}
