# Clinical Management System API

> A robust, scalable Backend API for clinical scheduling and management, built with a Modular Monolith architecture.

## 🚀 Overview

This project is a high-performance RESTful API designed to handle the complex operations of a medical clinic. It prioritizes security, data integrity, and scalability, employing modern software engineering practices such as SOLID principles, Data Transfer Objects (DTOs), and centralized exception handling.

## 🛠️ Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security & JWT (JSON Web Tokens)
* **Data Privacy:** AES-256 encryption for sensitive PII (Personally Identifiable Information) using JPA Converters (LGPD/GDPR Compliance)
* **Database:** MariaDB
* **Migrations:** Flyway
* **ORM:** Spring Data JPA / Hibernate
* **Build Tool:** Maven

## ✨ Core Features (Current State)

* **Stateless Authentication:** Secure login mechanism generating short-lived Access Tokens and long-lived HTTP-only Refresh Tokens.
* **Role-Based Access Control (RBAC):** Granular endpoint protection based on user profiles (e.g., `ROLE_ADMIN`, `ROLE_USER`).
* **Database Versioning:** Automated schema generation and data seeding using Flyway migrations.
* **User Management (CRUD):**
    * Creation with BCrypt password hashing.
    * Paginated listing with dynamic SpEL filtering.
    * Update mechanisms with conditional password encoding.
    * **Soft Delete:** Logical deactivation to preserve historical data and audit trails.
* **Global Exception Handling:** Standardized API error responses using `@ControllerAdvice`.

## 📦 Getting Started

### Prerequisites
* Java 21 (JDK)
* Maven 3.8+
* MariaDB Server

### Installation & Execution

1. Clone the repository:
   ```bash
   git clone [https://github.com/adrianojalves/clinica-api.git](https://github.com/adrianojalves/clinica-api.git)

2. Navigate to the project directory:

    ```bash
    cd clinica-api

3. Update the database credentials in src/main/resources/application.properties.

4. Run the application (Flyway will automatically create tables and insert the default Admin user):

    ```bash
    mvn spring-boot:run

### 🔒 Default Admin Credentials
    Upon the first initialization, the database is populated with a master administrator:
    Login: admin
    Password: 123456

(Ensure to change this or disable the default migration in a production environment).

### 🏗️ Architecture Notes
This project adopts a Modular Monolith approach. It keeps deployment simple while strictly separating domains (Auth, Users, Patients, Appointments) to allow an easy transition to microservices in the future if the scale demands it.