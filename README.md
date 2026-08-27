# Resource Booking System API

A production-quality, secure RESTful Resource Booking System built with **Java 17+**, **Spring Boot 3.3.x**, **Spring Security 6.x**, **JWT**, **Spring Data JPA**, and **PostgreSQL / MySQL / H2**.

---

## 1. Project Overview

The **Resource Booking System** allows organizations to manage and reserve physical or virtual assets (such as conference rooms, vehicles, lab stations, and recording equipment). 

### Key Highlights:
- **Stateless JWT Security**: Signed HMAC-SHA256 tokens with configurable secrets and expiration.
- **Strict Role-Based Access Control (RBAC)**: Supports `ROLE_USER` and `ROLE_ADMIN`.
- **Identity & Ownership Protection**: Authenticated user identity is always extracted from the `SecurityContext` / JWT. Clients cannot forge or inject `userId` or spoof reservation pricing.
- **Conflict Detection Engine**: Prevents double-booking using half-open interval semantics `[startTime, endTime)` across active (`PENDING`, `CONFIRMED`) reservations.
- **Server-Side Price Calculation**: Deterministic price computation using `BigDecimal` (`resource.price * durationHours`).
- **Dynamic Filtering & Pagination**: Filter reservations by status, price range, resource, and user ID with whitelisted sorting and pagination metadata.
- **Comprehensive OpenAPI 3.0 / Swagger UI**: Full interactive API documentation with Bearer token authentication.

---

## 2. Technology Stack

- **Language**: Java 17+ (Java 21/24 compatible)
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6.x, JJWT (`io.jsonwebtoken:jjwt-*` 0.12.6), BCrypt
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Databases**: PostgreSQL (default production), MySQL (supported), H2 (in-memory test/dev)
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@DecimalMin`, etc.)
- **Documentation**: Springdoc OpenAPI 2.6.0 / Swagger UI
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

---

## 3. Project Architecture

```
com.example.booking
├── config             # SecurityFilterChain, OpenApi, CORS, DataInitializer
├── controller         # Thin REST Controllers (Auth, Resource, Reservation)
├── dto
│   ├── auth           # LoginRequest, LoginResponse
│   ├── resource       # ResourceCreateRequest, ResourceUpdateRequest, ResourceResponse
│   ├── reservation    # ReservationCreateRequest, AdminReservationCreateRequest, ReservationUpdateRequest, ReservationResponse, ReservationStatusUpdateRequest
│   ├── user           # UserSummaryDto
│   └── common         # PagedResponse, ErrorResponse
├── entity             # User, Resource, Reservation (JPA entities with LAZY relations)
├── enums              # Role (USER, ADMIN), ReservationStatus (PENDING, CONFIRMED, CANCELLED)
├── exception          # Custom exceptions & @RestControllerAdvice GlobalExceptionHandler
├── mapper             # DTO <-> Entity mappers
├── repository         # JpaRepository and JpaSpecificationExecutor
├── security           # JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal, CustomUserDetailsService, EntryPoints
├── service            # Service interfaces
│   └── impl           # Service implementations with @Transactional
├── specification      # JPA Specifications for dynamic filtering
└── util               # PriceCalculator, SortUtils
```

---

## 4. Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Maven**: Version 3.8+ (or use the included `mvnw.cmd` wrapper)
- **Database**: PostgreSQL 14+ (or MySQL 8+, or use the embedded H2 database for local testing)

---

## 5. Database & Environment Configuration

Copy `.env.example` to `.env` or configure the following environment variables:

| Environment Variable | Description | Default Value |
| :--- | :--- | :--- |
| `DB_URL` | JDBC Database URL | `jdbc:postgresql://localhost:5432/booking_db` (or H2 fallback) |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `SERVER_PORT` | HTTP port | `8080` |
| `JWT_SECRET` | Secret key for signing JWTs (256-bit) | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION` | JWT token expiration time in milliseconds | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000,http://localhost:5173,http://localhost:8080` |

---

## 6. Building and Running

### Build Project
```bash
.\mvnw.cmd clean package
```

### Run Tests
```bash
.\mvnw.cmd clean test
```

### Run Application
```bash
.\mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## 7. Seeded Test Credentials

The application automatically seeds initial users, resources, and sample bookings on first boot:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | `ROLE_ADMIN` | Full CRUD privileges over all resources and reservations |
| `user` | `User@123` | `ROLE_USER` | Standard booking user account #1 |
| `john_doe` | `John@123` | `ROLE_USER` | Standard booking user account #2 (for isolation testing) |

---

## 8. Swagger / OpenAPI Documentation

Access Swagger UI directly in your browser:
```
http://localhost:8080/swagger-ui.html
```
Raw OpenAPI 3.0 spec:
```
http://localhost:8080/v3/api-docs
```

### Using Swagger UI with JWT Authentication:
1. Execute `POST /auth/login` with `{"username": "admin", "password": "Admin@123"}`.
2. Copy the `token` string from the response.
3. Click the **Authorize** button at the top right in Swagger UI.
4. Enter `Bearer <your_token>` and click **Authorize**.
5. All subsequent requests in Swagger will include the Authorization header.

---

## 9. Authorization Matrix

| Endpoint | Method | USER Role | ADMIN Role | Ownership & Business Rules |
| :--- | :--- | :--- | :--- | :--- |
| `/auth/login` | POST | Public | Public | Authenticates credentials and returns JWT |
| `/resources` | GET | Allowed | Allowed | Search and list available resources |
| `/resources/{id}` | GET | Allowed | Allowed | View single resource details |
| `/resources` | POST | 403 Forbidden | Allowed | Create new resource |
| `/resources/{id}` | PUT/PATCH | 403 Forbidden | Allowed | Modify resource |
| `/resources/{id}` | DELETE | 403 Forbidden | Allowed | Delete resource (blocked if active bookings exist) |
| `/reservations` | POST | Allowed | Allowed | USER: user identity derived from JWT; ADMIN: can specify `userId` |
| `/reservations` | GET | Allowed (Own) | Allowed (All) | USER receives only their own bookings; ADMIN receives all |
| `/reservations/{id}` | GET | Allowed (Own) | Allowed (All) | Returns 403 Forbidden if USER attempts another user's reservation |
| `/reservations/{id}` | PUT/PATCH | 403 Forbidden | Allowed | Update reservation interval and status |
| `/reservations/{id}/status` | PATCH | 403 Forbidden | Allowed | Update reservation status (PENDING, CONFIRMED, CANCELLED) |
| `/reservations/{id}/cancel` | PATCH | Allowed (Own) | Allowed | Cancels reservation if owner/admin and not already cancelled |
| `/reservations/{id}` | DELETE | 403 Forbidden | Allowed | Permanently delete reservation |

---

## 10. Pricing and Booking Conflict Rules

### Pricing Calculation Rule
Resource price represents an hourly rate.
```
durationHours = (endTime - startTime in minutes) / 60.0
totalPrice = resource.price * durationHours
```
Calculations use `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP` to prevent floating-point precision loss.

### Conflict Detection Semantics
- Reservations use half-open intervals: `[startTime, endTime)`.
- Statuses `PENDING` and `CONFIRMED` are **blocking**.
- Status `CANCELLED` is **non-blocking** (freeing the slot for other users).
- Back-to-back bookings (e.g. 10:00–12:00 and 12:00–14:00) are explicitly allowed.

---

## 11. cURL API Examples

### 1. Authenticate (Login)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "User@123"}'
```

### 2. List Resources with Filters
```bash
curl -X GET "http://localhost:8080/resources?type=ROOM&available=true&page=0&size=10&sort=price,asc" \
  -H "Authorization: Bearer <TOKEN>"
```

### 3. Create Resource (ADMIN only)
```bash
curl -X POST http://localhost:8080/resources \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Boardroom A",
    "description": "Executive 20-person boardroom",
    "type": "ROOM",
    "price": 200.00,
    "available": true
  }'
```

### 4. Create Reservation (USER)
```bash
curl -X POST http://localhost:8080/reservations \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "startTime": "2026-09-01T10:00:00",
    "endTime": "2026-09-01T12:00:00"
  }'
```

### 5. Get Own Reservations (USER)
```bash
curl -X GET "http://localhost:8080/reservations?status=CONFIRMED&minPrice=50&maxPrice=1000&page=0&size=10&sort=startTime,asc" \
  -H "Authorization: Bearer <USER_TOKEN>"
```

### 6. Cancel Reservation
```bash
curl -X PATCH http://localhost:8080/reservations/1/cancel \
  -H "Authorization: Bearer <USER_TOKEN>"
```

---

## 12. Concurrency & Locking Strategy

- Reservation creation and status updates execute within `@Transactional` boundaries.
- Active overlapping reservations are checked using database-level JPQL queries with index hints (`idx_reservation_resource_times`).
- In high-throughput clustered deployments, database pessimistic locking (`SELECT ... FOR UPDATE`) or unique exclusion constraints (such as PostgreSQL `gist` btree exclusion ranges) can be enabled to prevent concurrent race-condition overlap.
