package com.example.booking.config;

import com.example.booking.entity.Reservation;
import com.example.booking.entity.Resource;
import com.example.booking.entity.User;
import com.example.booking.enums.ReservationStatus;
import com.example.booking.enums.Role;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.util.PriceCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            ReservationRepository reservationRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Non-production profile active: initializing default seed data...");

            // 1. Seed Users
            User admin = userRepository.save(new User(
                    "admin",
                    "admin@example.com",
                    passwordEncoder.encode("Admin@123"),
                    Role.ADMIN
            ));

            User user1 = userRepository.save(new User(
                    "user",
                    "user@example.com",
                    passwordEncoder.encode("User@123"),
                    Role.USER
            ));

            User user2 = userRepository.save(new User(
                    "john_doe",
                    "john@example.com",
                    passwordEncoder.encode("John@123"),
                    Role.USER
            ));

            // 2. Seed Resources
            Resource r1 = resourceRepository.save(new Resource(
                    "Conference Room Alpha",
                    "Executive 12-person conference room with 4K display and video conferencing",
                    "ROOM",
                    new BigDecimal("150.00"),
                    true
            ));

            Resource r2 = resourceRepository.save(new Resource(
                    "Training Hall B",
                    "Large 50-seat workshop and training hall with audio system",
                    "ROOM",
                    new BigDecimal("300.00"),
                    true
            ));

            Resource r3 = resourceRepository.save(new Resource(
                    "Company EV Van #1",
                    "Electric passenger minivan for client transport and events",
                    "VEHICLE",
                    new BigDecimal("80.00"),
                    true
            ));

            Resource r4 = resourceRepository.save(new Resource(
                    "Professional 4K Studio Kit",
                    "High-end Sony 4K camera, tripod, lighting, and wireless mics",
                    "EQUIPMENT",
                    new BigDecimal("50.00"),
                    true
            ));

            Resource r5 = resourceRepository.save(new Resource(
                    "Podcast Recording Booth",
                    "Acoustically treated studio booth with Shure SM7B mics and interface",
                    "EQUIPMENT",
                    new BigDecimal("40.00"),
                    true
            ));

            // 3. Seed Reservations
            LocalDateTime baseTime = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);

            // User 1 reservations
            LocalDateTime u1Start1 = baseTime.plusHours(1);
            LocalDateTime u1End1 = baseTime.plusHours(3);
            reservationRepository.save(new Reservation(
                    r1,
                    user1,
                    u1Start1,
                    u1End1,
                    PriceCalculator.calculatePrice(r1.getPrice(), u1Start1, u1End1),
                    ReservationStatus.CONFIRMED
            ));

            LocalDateTime u1Start2 = baseTime.plusDays(1).withHour(14);
            LocalDateTime u1End2 = baseTime.plusDays(1).withHour(17);
            reservationRepository.save(new Reservation(
                    r3,
                    user1,
                    u1Start2,
                    u1End2,
                    PriceCalculator.calculatePrice(r3.getPrice(), u1Start2, u1End2),
                    ReservationStatus.PENDING
            ));

            LocalDateTime u1Start3 = baseTime.plusDays(3).withHour(10);
            LocalDateTime u1End3 = baseTime.plusDays(3).withHour(12);
            reservationRepository.save(new Reservation(
                    r4,
                    user1,
                    u1Start3,
                    u1End3,
                    PriceCalculator.calculatePrice(r4.getPrice(), u1Start3, u1End3),
                    ReservationStatus.CANCELLED
            ));

            // User 2 reservations
            LocalDateTime u2Start1 = baseTime.plusHours(4);
            LocalDateTime u2End1 = baseTime.plusHours(6);
            reservationRepository.save(new Reservation(
                    r1,
                    user2,
                    u2Start1,
                    u2End1,
                    PriceCalculator.calculatePrice(r1.getPrice(), u2Start1, u2End1),
                    ReservationStatus.CONFIRMED
            ));

            LocalDateTime u2Start2 = baseTime.plusDays(2).withHour(10);
            LocalDateTime u2End2 = baseTime.plusDays(2).withHour(15);
            reservationRepository.save(new Reservation(
                    r2,
                    user2,
                    u2Start2,
                    u2End2,
                    PriceCalculator.calculatePrice(r2.getPrice(), u2Start2, u2End2),
                    ReservationStatus.PENDING
            ));

            log.info("Development database seeding completed.");
        }
    }
}
