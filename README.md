# PrismaMed — Clinical Management System API

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-10.x-blue.svg?style=flat-square&logo=mariadb)](https://mariadb.org/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security%20%2B%20JWT-red.svg?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-Proprietary-darkgrey.svg?style=flat-square)]()

> A production-grade, modular-monolith REST API for medical clinic management, built with Spring Boot 3.x and Java 21.

---

## Table of Contents

- [Overview](#overview)
- [Key Features & Recent Improvements](#key-features--recent-improvements)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Setup](#local-setup)
  - [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
  - [Production Environment Variables](#production-environment-variables)
  - [Default Credentials](#default-credentials)
- [Architecture & Standards](#architecture--standards)
  - [Language Strict Rules](#language-strict-rules)
  - [Layer Structure](#layer-structure)
  - [Authentication Flow](#authentication-flow)
  - [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
  - [PII Encryption (LGPD / GDPR Compliance)](#pii-encryption-lgpd--gdpr-compliance)
  - [System Auditing (Logs)](#system-auditing-logs)
  - [Error Handling](#error-handling)
  - [Database Migrations](#database-migrations)
- [Domain Overview](#domain-overview)
  - [Company & Clinics](#company--clinics)
  - [Doctors & Patients](#doctors--patients)
  - [Procedures & Pricing (Excel Integration)](#procedures--pricing-excel-integration)
  - [Appointments (Atendimentos)](#appointments-atendimentos)
- [Reports & PDF Generation](#reports--pdf-generation)
  - [Report Layouts & Design Rules](#report-layouts--design-rules)
  - [Report Endpoints](#report-endpoints)
- [Development Commands](#development-commands)

---

## Overview

PrismaMed is a full-featured backend API designed to handle the complete operational and financial lifecycle of a medical clinic network. It manages patient registration, clinic-doctor-procedure pricing rules, appointment scheduling, billing, split commissions (repasse), and robust analytical reporting.

An Angular SPA is served directly as static resources alongside the API, with all non-API web routes forwarded to `index.html` via a customized `SpaWebMvcConfigurer`.

---

## Key Features & Recent Improvements

- **System Auditing & History Logs**: Tracks critical events and administrative operations (e.g. deletion of open appointments) in a dedicated `logs` table, accessible via `/api/clinica/logs` (admin-only).
- **Excel Batch Import & Export**: Supports importing and exporting both the global medical procedures catalog and the specific clinic-doctor pricing configuration from/to Excel files (`.xlsx`), enabling fast data setup.
- **Dynamic Guide Codes (`codigoGuia`)**: Allows associating unique insurance or system guide codes directly with Clinics and specific Appointments.
- **Procedure Searching tags**: Integrates search tags (`tag` field) in `MedicalProcedure` for rapid, tag-based procedure discovery inside care modules.
- **LGPD/PII Data Security**: Protects sensitive patient data at rest using AES-256-GCM. Uses a fast SHA-256 hash index for lookups.
- **Advanced PDF Reports**: Automatically generates stylized A4/A5 PDF documents (Daily summaries, Clinic Performance, ABC Procedure Analysis, Commissions/Repasse by payment periods, Referral guides with full patient address, and payment receipts). Includes an "Open Appointment" visual indicator if the appointment has not yet been finalized.

---

## Technology Stack

| Layer | Technology / Library | Purpose |
|---|---|---|
| **Core Framework** | Java 21, Spring Boot 3.x | Runtime and application base |
| **Security** | Spring Security + Auth0 Java JWT | JWT Authentication and authorization |
| **Persistence** | Spring Data JPA / Hibernate | Object-Relational Mapping (ORM) |
| **Database** | MariaDB | Primary relational database |
| **Migrations** | Flyway | Structured SQL schema versioning |
| **Excel Parser** | Apache POI / POI-OOXML (v5.2.5) | Import & Export data as `.xlsx` spreadsheets |
| **PDF Generation** | Thymeleaf + OpenHTMLtoPDF | Conversions of HTML templates into PDFs |
| **API Docs** | Springdoc OpenAPI (Swagger UI) | Interactive REST API documentation |

---

## Getting Started

### Prerequisites

- **Java 21 (JDK)**
- **Maven 3.8+**
- **MariaDB** (instance with a database named `clinica_db` by default)

### Local Setup

1. **Clone the repository** and navigate to the project directory.
2. **Configure properties**: Copy the dev properties template and populate it with your local credentials, a 32-byte JWT secret, and a 32-byte hash encryption key:
   ```bash
   cp src/main/resources/application-dev.properties.example \
      src/main/resources/application-dev.properties
   ```
   *Required settings inside `application-dev.properties`:*
   - `spring.datasource.username`: Your MariaDB username
   - `spring.datasource.password`: Your MariaDB password
   - `api.security.token.secret`: Your 32-byte (or longer) JWT signing secret key
   - `api.security.crypto.key`: Your 32-byte secret key for GCM encryption (LGPD)

3. **Start the Database**: Ensure MariaDB is running with the configured schema database (`clinica_db`). Flyway automatically runs all migrations and seeds default configurations and users on first run.

4. **Launch the Application**:
   ```bash
   # Development profile (active sql logging, insecure cookies for localhost)
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

   # Production profile (uses environment variables)
   ./mvnw spring-boot:run
   ```

### API Documentation (Swagger UI)

When the application is running, the interactive Swagger UI API documentation is available at:
* **Swagger UI URL:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Raw OpenAPI Specification:** `http://localhost:8080/v3/api-docs`

### Production Environment Variables

In production, Spring Boot will bind the configurations defined in `application.properties` from the environment. Ensure these variables are defined in your deployment environment:

- `DB_HOST`: Hostname of the MariaDB database
- `DB_PORT`: Database port (defaults to `3306`)
- `DB_NAME`: Database schema name
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key used for signing JWT tokens
- `HASH_SECRET`: Secret key used for AES-256-GCM encryption of sensitive fields
- `API_SECURITY_COOKIE_SECURE`: Set to `true` to require HTTPS for refresh token cookies
- `API_CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins (e.g. `https://clinica.mycorp.com`)

### Default Credentials

Flyway seeds a default administrator account on setup:

| Username | Password | Default Roles |
|---|---|---|
| `admin` | `123456` | `ADMIN` |

> [!WARNING]
> Change the default admin credentials immediately in any non-local/production deployment.

---

## Architecture & Standards

The application is structured as a **Modular Monolith** to keep domains segregated and highly cohesive, allowing straightforward transition to microservices if needed.

### Language Strict Rules

To maintain high standards, the repository follows a strict double-language rule:
- **Internal Source Code (English)**: All code assets (class names, methods, variables, parameters, comments, database schemas, tables, and columns) must be written exclusively in **English**.
- **User-Facing Strings (Brazilian Portuguese)**: All client-facing elements (validation constraints, API error messages, custom exceptions, logs, and generated PDF reports/documents) must be written in **Português (Brasil)**.

### Layer Structure

Domain code resides within `br.com.ajasoftware.clinica` structured by layers:

```
br.com.ajasoftware.clinica/
│
├── controller/         # REST Controllers: handle requests, define @PreAuthorize checks
├── service/            # Service layer: contains transactional business logic (@Service)
├── repository/         # Data layer: Spring Data JPA interfaces (flat layout)
│
└── domain/
    ├── entity/         # JPA Entities (incorporate soft-delete & audit lifecycle hooks)
    ├── dto/            # Data Transfer Objects (Java Records: Request, Response, Update)
    └── filter/         # Query criteria extending FilterBase for dynamic JPA paging
```

### Authentication Flow

1. **User Login (`POST /api/auth/login`)**: Validates credentials. Returns a short-lived access token (30 minutes) in the JSON body, and sets a secure, HTTP-only refresh token cookie (7 days) restricted to `/api/auth/refresh`.
2. **Request Validation**: The `SecurityFilter` intercepts every incoming request. It extracts and verifies the `Authorization: Bearer <token>` header, authenticating the request stateless.
3. **Token Refresh (`POST /api/auth/refresh`)**: Validates the HTTP-only cookie and issues a brand-new access token and refresh cookie pair.
4. **Logout (`POST /api/auth/logout`)**: Expires the refresh token cookie.

### Role-Based Access Control (RBAC)

Endpoints utilize method-level authorization with `@PreAuthorize`. The system supports four security roles:

- `ADMIN`: Full application access. Authorized to perform administrative actions (e.g. database purge, deleting open appointments, auditing logs).
- `CADASTROS`: Manage base registers (Clinics, Doctors, Patients, Procedures, and general Pricing lists).
- `ATENDIMENTO`: Care operations (create, update appointments, manage items, register client payments, print receipts).
- `RELATORIOS`: Read-only access to analytical PDF reports and clinic performance data.

### PII Encryption (LGPD / GDPR Compliance)

To meet data protection laws (such as LGPD in Brazil), sensitive personal information in the `Client` (Patient) table is encrypted at rest using **AES-256-GCM** via the `CryptoConverter` JPA attribute converter.
- **Encrypted Fields:** CPF, RG, phone, email, biological sex, and sexual orientation.
- **Indexed Equality Lookups:** Because AES-256-GCM ciphertext changes, fields like CPF cannot be direct-matched. The system stores a SHA-256 hash of the value (`cpf_hash`) generated by `SysClinicaUtils.generateSha256(rawCpf)`. Equal-based queries query the database using this hash representation.

### System Auditing (Logs)

Critical operations and database changes trigger auditing logs stored in the `log` schema.
- **Log Schema:** `id`, `user` (who performed the action), `action_description`, `creation_date`.
- **Endpoint:** `/api/clinica/logs` (support paging and sorting, restricted to `ADMIN` only).
- **Core Event Trigger:** When an `ADMIN` deletes an open appointment, an entry is written into the audit log register.

### Error Handling

The central `GlobalExceptionHandler` interceptor standardizes the API response formats:
- **Validation Failures (`MethodArgumentNotValidException`)**: Returns a status code `400` containing an array of field errors: `[{ "field": "email", "message": "O e-mail informado é inválido." }]`.
- **Domain Business Violations (`BusinessException`)**: Returns a status code `400` in the structured array: `[{ "field": "global", "message": "Regra de negócio violada..." }]`.
- **HTTP status exceptions (`ResponseStatusException`)**: Returns the corresponding HTTP code (e.g., `404 Not Found`) containing the message.

### Database Migrations

Database modifications are managed by Flyway. 
- **Migration Location**: `src/main/resources/db/migration/YYYY/MM/`
- **Naming Rule**: Files must be named with a 5-digit sequence prefix and description in English (e.g. `V00025__add_codigo_guia_to_clinic_and_atendimento.sql`).
- **Safety Policy**: Never alter already-applied migration files. Always append a new migration script to execute schema mutations.

---

## Domain Overview

### Company & Clinics

- **Company Profile (`/api/clinica/company`)**: A single corporate tenant registration (Corporate Name, CNPJ, Contact, and Company logo binary). The logo is served as a Base64 string for PDF rendering.
- **Clinic Network (`/api/clinica/clinics`)**: Supports multiple clinical branches.
  - **Payment Period (`periodPayment`)**: Configurable commission payment schedules for doctors per clinic using the `PeriodPayment` enum (`DIARIO`, `SEMANAL`, `QUINZENAL`, `MENSAL`).
  - **Guide Code (`codigoGuia`)**: A numerical guide identifier mapping the clinic branch inside health insurance systems.

### Doctors & Patients

- **Doctor Registry (`/api/clinica/doctors`)**: Doctor record detailing professional medical licensing (CRM), specialty type, active status toggle, and soft-delete toggle.
- **Patients / Clients (`/api/clinica/clients`)**: Patient details. Sensitive columns are automatically encrypted by the `CryptoConverter` JPA engine. Enforces CPF uniqueness using the SHA-256 `cpf_hash` field.

### Procedures & Pricing (Excel Integration)

- **Medical Procedures (`/api/clinica/procedures`)**: The medical services catalog classified by type (e.g., `CONSULTA`, `EXAME`).
  - **Tags (`tag`)**: An optional field for categorization and rapid searching inside care modules.
  - **Excel Operations**: Access `GET /api/clinica/procedures/export` to download the spreadsheet catalog, edit names, descriptions, or tags, and re-upload via `POST /api/clinica/procedures/import`.
- **Clinic–Doctor–Procedure Pricing (`/api/clinica/clinic-procedures`)**: Defines the pricing catalog linking specific Clinics, optional Doctors, and Procedures.
  - **Pricing options**: Supports cash pricing (`price`), card pricing (`priceCard`), and partner pricing (`pricePartner`, indicating price charged/collected by the clinic branch).
  - **Excel Operations**: Download pricing worksheets using `GET /api/clinica/clinic-procedures/export?clinicId={id}`, edit pricing columns (`price`, `priceCard`, `pricePartner`, `codigoClinica`), and batch-upload the spreadsheet using `POST /api/clinica/clinic-procedures/import`.

### Appointments (Atendimentos)

The care module is the core transactional unit:
- **Guide Code (`codigoGuia`)**: Associates specific health insurer guides to appointments.
- **Item Entries (`/api/clinica/atendimentos/{id}/itens`)**: Links specific pricing from the `ClinicDoctorProcedure` matrix. Prices and doctors are locked inside the line items once added to prevent historical alterations.
- **Payment Entries (`/api/clinica/atendimentos/{id}/pagamentos`)**: Tracks dynamic payment methods (cash, card, split installments) and discounts.
- **Status Lifecycle**: Starts as `EM_ABERTO` (Open). When execution completes, it is transitioned to `ENCAMINHADO` (Finalized) via `POST /api/clinica/atendimentos/{id}/finalizar`, locking changes.
- **Purge Policies**: Finalized appointments are read-only. Open appointments (`EM_ABERTO`) can only be deleted by users holding the `ADMIN` role. This action is permanently audited in the system logs.

---

## Reports & PDF Generation

PrismaMed uses Thymeleaf parsing coupled with the **OpenHTMLtoPDF** layout engine to convert stylized HTML/CSS templates into PDF documents.

### Report Layouts & Design Rules

- **Header Open Status**: If a document (Referral Guide/Receipt) is printed for an appointment that is still `EM_ABERTO`, a visual notification warning ("ATENDIMENTO EM ABERTO - NÃO FINALIZADO") is automatically rendered in the PDF header.
- **Page Rules**: Margin definitions, page numbers, and size specifications are managed inside CSS using `@page` rules.
  - **A5 Portrait**: Care Module outputs (Guia de Encaminhamento and Recibos).
  - **A4 Landscape/Portrait**: Analytical, synthetic, and financial reports.
- **Typography & Alignment**: Column text alignment is managed strictly using column-specific helper classes (e.g. `.col-valor { text-align: right }`) applying to both `th` and `td` to guarantee correct alignment.
- **Theme Palette (PrismaMed CSS)**:
  - Primary Blue: `#005293` (Branding, tables, titles)
  - Teal Green: `#009B8E` (Highlights, borders)
  - Off-White Background: `#F4F7F8` (Table headers, shaded containers)
  - Charcoal Gray: `#2D3748` (Primary body text)
  - Slate Gray: `#718096` (Secondary metadata, label texts)

### Report Endpoints

| Report Description | Endpoint Path | PDF Format | Role Allowed |
|---|---|---|---|
| **Referral Guide** | `GET /api/clinica/atendimentos/{id}/encaminhamento` | A5 Portrait | `ATENDIMENTO`, `ADMIN` |
| **Payment Receipt** | `GET /api/clinica/atendimentos/{id}/recibo` | A5 Portrait | `ATENDIMENTO`, `ADMIN` |
| **Appointments Summary** | `GET /api/clinica/atendimentos/relatorios` | A4 Landscape | `RELATORIOS`, `ADMIN` |
| **Daily Appointments** | `GET /api/clinica/atendimentos/relatorios/atendimento-diario` | A4 Landscape | `RELATORIOS`, `ADMIN` |
| **Doctor Commissions (Repasse)** | `GET /api/clinica/repasse/relatorios` | A4 Landscape | `RELATORIOS`, `ADMIN` |
| **ABC Procedure Analysis** | `GET /api/clinica/procedimentos/relatorios/abc` | A4 Landscape | `RELATORIOS`, `ADMIN` |
| **Clinic Performance Dashboard** | `GET /api/clinica/desempenho/relatorios` | A4 Landscape | `RELATORIOS`, `ADMIN` |
| **Patient Medical History** | `GET /api/clinica/pacientes/relatorios/historico` | A4 Landscape | `RELATORIOS`, `ADMIN` |

---

## Development Commands

```bash
# Run all unit and integration tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=ClinicControllerTest

# Run a specific test method
./mvnw test -Dtest=ClinicControllerTest#shouldCreateClinicSuccessfully

# Package the application (produces standard JAR at target/aplicativo.jar)
./mvnw clean package
```

