# PrismaMed — Clinical Management System API

> A full-featured, modular-monolith REST API for medical clinic management, built with Spring Boot 3.x and Java 21.

## Overview

PrismaMed is a production-ready backend API that handles the complete operational lifecycle of a medical clinic network: patient registration with LGPD-compliant PII encryption, appointment scheduling and execution, multi-clinic procedure pricing, financial controls (add-ons, discounts, payment installments), document generation, and a suite of analytical PDF reports.

An Angular SPA (pre-built) is served as static resources alongside the API, with all non-API routes forwarded to `index.html` via `SpaWebMvcConfigurer`.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (stateless, HTTP-only refresh cookie) |
| ORM | Spring Data JPA / Hibernate |
| Database | MariaDB |
| Migrations | Flyway |
| PDF Generation | Thymeleaf + OpenHTMLtoPDF |
| Data Privacy | AES-256-GCM encryption via JPA Converter (LGPD) |
| Build Tool | Maven |

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven 3.8+
- MariaDB (database `clinica_db` by default)

### Local Setup

1. Copy the development properties template and fill in your credentials:
   ```bash
   cp src/main/resources/application-dev.properties.example \
      src/main/resources/application-dev.properties
   ```
   Required values: MariaDB host/port/name/user/password, a 32-byte `JWT_SECRET`, and a 32-byte `HASH_SECRET`.

2. Start MariaDB. Flyway will create all tables and seed initial data automatically on first boot.

3. Run the application:
   ```bash
   # Development mode (verbose SQL, insecure cookies)
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

   # Production mode
   ./mvnw spring-boot:run
   ```

### Production Environment Variables

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `HASH_SECRET`

### Default Admin Credentials

| Field | Value |
|---|---|
| Login | `admin` |
| Password | `123456` |

Change or disable the default migration in any production environment.

## Architecture

The application follows a **Modular Monolith** pattern — a single deployable JAR with strict domain separation, making a future migration to microservices straightforward if scale demands it.

### Layer Structure (`br.com.ajasoftware.clinica`)

| Layer | Package | Role |
|---|---|---|
| Controllers | `controller/<domain>` | REST endpoints; `@PreAuthorize` for RBAC |
| Services | `service/<domain>` | Business logic; `@Transactional` boundaries |
| Repositories | `repository/` | Spring Data JPA interfaces with JPQL projections |
| Entities | `domain/entity/<domain>` | JPA entities with soft-delete and audit fields |
| DTOs | `domain/dto/<domain>` | `*RequestDTO`, `*ResponseDTO`, `*UpdateDTO` |
| Filters | `domain/filter/<domain>` | Extend `FilterBase`; used for dynamic query parameters |
| Security | `security/` | `SecurityConfigurations`, `SecurityFilter` (JWT interceptor) |
| Infrastructure | `infrastructure/` | `CryptoUtils` (AES-256-GCM), `CryptoConverter` (JPA), `SpaWebMvcConfigurer` |
| Reports | `service/relatorio/` | PDF generation services backed by Thymeleaf templates |
| Exceptions | `exceptions/` | `GlobalExceptionHandler`, `BusinessException` |

### Authentication Flow

- `POST /api/auth/login` — returns a short-lived **access token** (30 min) in the JSON body and sets a **refresh token** (7 days) as an HTTP-only cookie restricted to `/api/auth/refresh`.
- `SecurityFilter` validates `Authorization: Bearer <token>` on every request. Returns `401` on expiry.
- `POST /api/auth/refresh` — issues a new token pair using the cookie.

### Role-Based Access Control

| Role | Access |
|---|---|
| `ADMIN` | Full access to all endpoints |
| `CADASTROS` | Clinics, doctors, patients, procedures, pricing |
| `ATENDIMENTO` | Care management (appointments, items, payments) |
| `RELATORIOS` | All PDF report endpoints and user summary lookup |

### PII Encryption (LGPD Compliance)

Sensitive `Client` fields (`cpf`, `rg`, `phone`, `email`, `biologicalSex`, `sexualOrientation`) are encrypted at rest using **AES-256-GCM** via the `CryptoConverter` JPA attribute converter. CPF is additionally stored as a SHA-256 hash (`cpf_hash`) to allow fast equality-based lookups without decryption.

### Error Handling

`GlobalExceptionHandler` standardizes all error responses:

- `MethodArgumentNotValidException` / `ConstraintViolationException` → `400` with `[{field, message}]`
- `BusinessException` → `400` with `[{field: "global", message: "..."}]`
- `ResponseStatusException` → mirrors its HTTP status

### Database Migrations

Flyway migrations live under `src/main/resources/db/migration/YYYY/MM/` using the naming scheme `V{5-digit-seq}__{description}.sql`. Always add new files — never modify existing ones.

## Domain Overview

### Company (`/api/clinica/company`)
Single-tenant company profile: corporate name, CNPJ, address, contact details, and logo (stored as binary, served as Base64 for PDF headers).

### Clinics (`/api/clinica/clinics`)
Multi-clinic support with address, contact, CNPJ, and per-clinic transfer percentage. Soft-delete enabled.

### Doctors (`/api/clinica/doctors`)
Doctor registry with CRM, specialty, and status control. Soft-delete enabled.

### Patients (`/api/clinica/clients`)
Full patient record with LGPD-encrypted PII fields, CPF uniqueness enforced via hash, and CPF-based lookup endpoint.

### Medical Procedures (`/api/clinica/procedures`)
Procedure catalog with type classification (`CONSULTA` or exam/procedure variants). Hard-delete supported.

### Clinic–Doctor–Procedure Pricing (`/api/clinica/clinic-procedures`)
Central many-to-many-with-attributes table linking a clinic, an optional doctor, and a procedure with independent cash and card pricing. This is the pricing source for all appointments.

### Appointments — Atendimento (`/api/clinica/atendimentos`)

The core operational domain:

- **Create / Update / Delete** a care record linking a patient, clinic, and shift.
- **Add / list items** (`/{id}/itens`) — each item references a `ClinicDoctorProcedure` with its locked price, optional doctor, and quantity.
- **Payments** (`/{id}/pagamentos`) — multiple payment entries per appointment (cash, card, installments); CRUD supported.
- **Finalize** (`POST /{id}/finalizar`) — transitions status to `ENCAMINHADO`, locking the record for reporting.
- **Referral guide PDF** (`GET /{id}/encaminhamento`) — A5 portrait, inline PDF.
- **Payment receipt PDF** (`GET /{id}/recibo`) — A5 portrait, inline PDF.

### Reports — PDF Generation

All reports target `status = ENCAMINHADO` records only. Filters are passed as query parameters; status is never exposed to the frontend.

| Report | Endpoint | Format |
|---|---|---|
| Appointments (summary + analytical) | `GET /api/clinica/atendimentos/relatorios` | A4 landscape |
| Transfer (Repasse) by Clinic | `GET /api/clinica/repasse/relatorios` | A4 landscape |
| ABC Procedure Analysis | `GET /api/clinica/procedimentos/relatorios/abc` | A4 landscape |
| Clinic Performance Dashboard | `GET /api/clinica/desempenho/relatorios` | A4 landscape |
| Patient History | `GET /api/clinica/pacientes/relatorios/historico` | A4 landscape |

### Users & Roles (`/api/clinica/users`, `/api/clinica/role`)
User management with BCrypt-hashed passwords, role assignment, status toggle, and paginated listing. Admin-only.

### Administrator (`/api/admin`)
Admin-level bulk operations (e.g., full data reset). Admin-only.

## Development Commands

```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClinicControllerTest

# Run a single test method
./mvnw test -Dtest=ClinicControllerTest#shouldCreateClinicSuccessfully

# Build JAR
./mvnw package
```
