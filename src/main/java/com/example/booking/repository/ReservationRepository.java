package com.example.booking.repository;

import com.example.booking.entity.Reservation;
import com.example.booking.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
           "WHERE r.resource.id = :resourceId " +
           "AND r.status IN :statuses " +
           "AND r.startTime < :endTime " +
           "AND r.endTime > :startTime " +
           "AND (:excludeReservationId IS NULL OR r.id <> :excludeReservationId)")
    boolean existsConflictingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("excludeReservationId") Long excludeReservationId
    );

    long countByResourceIdAndStatusIn(Long resourceId, Collection<ReservationStatus> statuses);

    long countByResourceId(Long resourceId);

    @Override
    @EntityGraph(attributePaths = {"resource", "user"})
    Optional<Reservation> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"resource", "user"})
    Page<Reservation> findAll(Specification<Reservation> spec, Pageable pageable);
}
