# User Management Service  
**Spring Boot 3.x | JWT | MySQL | Kafka | Dockerized Backend Assignment**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Kafka](https://img.shields.io/badge/Event%20Broker-Kafka-yellow)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 🧩 Overview
This project implements a **User Management System with Role-Based Access Control (RBAC)**, built with **Spring Boot 3**, **MySQL**, and **Kafka** for event publishing.  
It demonstrates secure authentication using **JWT**, modular layered architecture, and production-ready DevOps setup via Docker Compose.

### System Architecture Diagram:
![System Architecture](./assets/system-architecture.png)

### **Tech Stack**
- **Framework:** Spring Boot 3.x  
- **Language:** Java 17  
- **Database:** MySQL  
- **Broker:** Apache Kafka  
- **Security:** Spring Security, JWT, BCrypt  
- **Logging:** Log4j2  
- **Caching:** Spring Cache  
- **Validation:** JSR-380 / Bean Validation  
- **Docs:** Swagger / OpenAPI  
- **Build Tool:** Maven  
- **Containerization:** Docker + Docker Compose  


### ⚙️ Dependency Overview

This project follows a **modular and layered dependency design** — separating application concerns across web, security, data, messaging, and developer utilities.  
Each dependency serves a specific purpose in the overall architecture and supports clean scalability, observability, and maintainability.

🧱 Core Spring Framework

- **spring-boot-starter-web** – Enables RESTful API development using Spring MVC.  
- **spring-boot-starter-security** – Provides authentication, authorization, and filter-based JWT security.  
- **spring-boot-starter-data-jpa** – Simplifies ORM mapping and database persistence using Hibernate.  
- **spring-boot-starter-validation** – Adds JSR-380 bean validation for request and DTO classes.  
- **spring-boot-starter-cache** – Enables caching through annotations to improve performance for frequent lookups.

🗄️ Database Layer

- **mysql-connector-j** – JDBC driver for connecting to the MySQL database.

☁️ Messaging Integration

- **spring-kafka** – Enables Kafka-based event-driven communication for user registration and login event streaming.

🔐 Security & Authentication

- **spring-security-crypto** – Provides utilities for secure password hashing using BCrypt.  
- **jjwt-api**, **jjwt-impl**, **jjwt-jackson** – Implements JWT creation, signing, and validation for stateless authentication.

🧩 Data Serialization & Interoperability

- **jackson-databind** – Handles JSON serialization and deserialization for API payloads.  
- **jackson-dataformat-xml** – Adds XML support for message transformation and queue communication.  
- **jackson-datatype-jsr310** – Provides proper serialization for Java 8+ date/time classes such as `LocalDateTime`.  
- **jakarta.xml.bind-api**, **jaxb-runtime** – Supports XML marshalling/unmarshalling via JAXB for AWS XML-based payloads.

🧭 API Documentation

- **springdoc-openapi-starter-webmvc-ui** – Generates OpenAPI 3 documentation and exposes Swagger UI for easy API exploration.

🧮 DTO Mapping

- **mapstruct**, **mapstruct-processor** – Generates compile-time DTO ↔ Entity mappers for a cleaner service layer.

🧾 Logging & Monitoring

- **log4j-core**, **log4j-slf4j2-impl** – Replaces Spring Boot’s default logging with Log4j2 for improved performance and async logging.  
- **log4j-bom** – Ensures consistent version alignment across all Log4j components.

⚙️ Developer Productivity

- **lombok** – Reduces boilerplate in entity and DTO classes (getters, setters, builders).  
- **spring-boot-devtools** – Provides hot reload and live restart features during local development.

🧪 Testing Frameworks

- **spring-boot-starter-test** – Bundles JUnit 5, Mockito, and MockMvc for unit and integration testing.  
- **spring-security-test** – Allows secure endpoint testing with mock authentication contexts.

✅ Summary

This dependency configuration provides:
- A **secure, JWT-based authentication** system.  
- **Event-driven messaging** via Kafka.  
- **XML and JSON compatibility** for flexible payload handling.  
- **Robust database persistence** with JPA and MySQL.  
- **Logging and caching optimizations** for production stability.  
- **End-to-end test coverage** for both unit and integration layers.  
- **Developer-friendly tools** like Lombok, DevTools, and Swagger UI.

## 📁 Project Structure (Package Overview)
```
com.visitly
│
├── VisitlyAssessment.java
│
├── config
│   ├── KafkaTopicConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
│
├── controller
│   ├── AdminController.java
│   ├── RoleController.java
│   └── UserController.java
│
├── dto
│   ├── ApiResponse.java
│   ├── LoginRequest.java
│   ├── RegisterUserRequest.java
│   ├── RoleResponse.java
│   ├── TokenResponse.java
│   ├── UserProfileResponse.java
│   └── UserRoleResponse.java
│
├── events
│   └── UserEvent.java
│
├── exception
│   └── GlobalExceptionHandler.java
│
├── mapper
│   ├── RoleMapper.java
│   ├── UserMapper.java
│   └── UserRoleMapper.java
│
├── model
│   ├── AuditLog.java
│   ├── Role.java
│   ├── User.java
│   └── UserRole.java
│
├── repository
│   ├── AuditLogRepository.java
│   ├── RoleRepository.java
│   ├── UserRepository.java
│   └── UserRoleRepository.java
│
├── security
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
│
└── service
    ├── AdminService.java
    ├── AuditService.java
    ├── RoleService.java
    ├── UserService.java
    └── kafka
        ├── KafkaConsumerService.java
        └── KafkaProducerService.java
```



## ✅ Functional Checklist

### 🚀 **API Endpoints**
```
+-------------------------------------------------------------------------------------------------------------------------------+
| Endpoint                    | Method   | Description                                             | Auth Required    | Status  |
|-----------------------------|----------|---------------------------------------------------------|------------------|-------- |
| `/api/users/register`       |   POST   | Register a new user with a hashed password              | NO               | Active  |
| `/api/users/login`          |   POST   | Authenticate user and return a JWT token                | NO               | Active  |
| `/api/users/me`             |   GET    | Fetch the current authenticated user's profile          | YES              | Active  |
| `/api/roles`                |   POST   | Create new roles (e.g., `ADMIN`, `USER`)                | YES (Admin only) | Active  |
| `/api/users/{userId}/roles` |   POST   | Assign one or more roles to a user                      | YES (Admin only) | Active  |
| `/api/admin/stats`          |   GET    | Retrieve system statistics (total users, recent logins) | YES (Admin only) | Active  |
+-------------------------------------------------------------------------------------------------------------------------------+
```

Notes:
- For auth required:
	- `YES` = Implemented and working  
	- `NO` = Public endpoint (no authentication required)  
- All secured endpoints use **JWT authentication** via `Authorization: Bearer <token>` header.  
- Admin-only endpoints require a valid token with the `ROLE_ADMIN` authority.



### **Technical Requirements**
```
+--------------------------------------------------------------------+
| Category         | Implementation                                  |
|------------------|-------------------------------------------------|
| Architecture     | Layered (Controller → Service → Repository)     |
| Security         | JWT + BCrypt + RBAC                             |
| Validation       | Spring Validation (`@Valid`)                    | 
| Error Handling   | Global Exception Handling (`@ControllerAdvice`) |
| DTO Mapping      | Manual mapping between entities and DTOs        |
| Audit Logging    | Kafka events on login and role assignment       |
| Caching          | Applied on `/api/users/me`                      |
| Testing          | Unit (Mockito) + Integration (MockMvc)          |
+--------------------------------------------------------------------+
```

---

### **DevOps / Docker**
- **Dockerfile** for Spring Boot application  
- **docker-compose.yml** includes:
  - `visitly-app` (Spring Boot)
  - `mysql` (User DB)
  - `kafka` (Event Broker)
- Kafka events published on:
  - User Registration  
  - User Login  
- MySQL auto-migration via JPA  
- Log4j2 configured for structured logging  

---

### **Bonus Features**
✅ Swagger UI for API documentation  
✅ Lombok for boilerplate reduction  
✅ Audit event publishing to Kafka  
✅ Caching on profile endpoint  
✅ Integration tests for secured endpoints  

---

## ⚙️ Setup Instructions (Docker Only)

This project is fully containerized — no local installation required.  
The application, database, and Kafka broker all run inside **Docker containers** using **Docker Compose**.

---

### **1️⃣ Prerequisites**

Before you begin, make sure the following are installed on your system:

- **Docker** (v20+ recommended)  
- **Docker Compose** (v2+)

To verify your setup:

``` docker -v
docker compose version
```

### **2️⃣ Clone the Repository**

Clone the User Management Service repository:

``` 
git clone https://github.com/tyagiab3/user-service.git
cd user-service
```

### **3️⃣ Start the Application**


Run all containers with one command:

``` 
docker compose up --build
```


This command will:

1. Build the Spring Boot application image (from the included Dockerfile)

2. Pull and start dependent services — MySQL and Kafka

3. Start all containers together under a shared Docker network

You’ll see logs for:

- mysql initializing the database (port 3306)

- kafka starting the broker (port 9092)

- visitly-app running the Spring Boot service on port 8080

### **4️⃣ Verify Running Containers**

Once startup completes, confirm all services are active:
```
docker ps
```

You should see something like:

```
CONTAINER NAME   STATUS   PORTS
visitly-app        Up     0.0.0.0:8080→8080/tcp
kafka              Up     0.0.0.0:9092→9092/tcp
zookeeper          Up     0.0.0.0:2181→2181/tcp
mysql              Up     0.0.0.0:3306→3306/tcp
```
### **5️⃣ Access the Application**

Once all services are running:

Swagger UI: http://localhost:8080/swagger-ui.html

![Swagger UI](./assets/Swagger-OpenAPI.jpg)

API Base URL: http://localhost:8080/api

Database: Available on port 3306 (root:root)

Kafka Broker: Available on port 9092

### **6️⃣ Stopping the Application**

When you’re done, gracefully shut down all containers:

```
docker compose down
```

To remove all containers, networks, and volumes (reset state completely):

```
docker compose down -v
```

### **7️⃣ Common Troubleshooting**
**🧱 1. Port Conflicts**

If MySQL or Kafka ports are already in use:

Stop any local MySQL/Kafka instances.
Or, modify the ports in docker-compose.yml.

**🐢 2. Slow Startup on First Run**

Kafka and MySQL take a few extra seconds to initialize on first launch.
The Spring Boot container automatically retries connections until dependencies are ready.

**🔁 3. Rebuilding After Code Changes**

If you change Java code and want to rebuild the Docker image:

```
docker compose build visitly-app
docker compose up
```
### **8️⃣ Verifying Events and Data**

Registration & Login Events: Automatically published to Kafka (user-registration-events & user-login-events topic).

Database Persistence: MySQL container stores user and role data.

Logging: Output visible in application logs (visitly-app).

Audit Logging: The MySQL database also contains an audit log table that logs activity.

### **9️⃣ Docker Compose Service Summary**

```
+----------------------------------------------------------------------------------------------+
|Service     | Purpose                             |Ports |Notes                               |
|------------|-------------------------------------|------|------------------------------------|
|visitly-app | Spring Boot REST API                | 8080 | Main application                   |
|mysql       | Relational DB for user data         | 3306 | Auto-initialized schema            |
|zookeeper   | Kafka dependency                    | 2181 | Internal use                       |
|kafka       | Message broker for event publishing | 9092 | Receives registration/login events |
+----------------------------------------------------------------------------------------------+
```

### **1️⃣0️⃣ Initial Database Setup & Admin Configuration**

When the containers start for the first time, the **MySQL database schema and initial data** are created automatically through the included **`init.sql`** script.

#### **⚙️ What `init.sql` Does**
The script:
- Creates the `visitlydb` database.
- Defines core tables: `users`, `roles`, and `user_roles`.
- Inserts a default **ADMIN** role.
- Inserts a sample user (`test1@example.com`) and assigns it the ADMIN role.

#### **⚠️ Important Note about the Default User**
While `init.sql` inserts a user record with what appears to be a hashed password, that hash **was not generated by the same BCrypt configuration used in the application**.  
Because of this mismatch, Spring Security cannot verify it during login, and **the default user cannot be used for authentication**.

#### **✅ To Get Started Correctly**
You need to create your own admin user through the API and manually assign the ADMIN role.  

- **Register a new user** using the `/api/users/register` endpoint:

```
   curl -X POST http://localhost:8080/api/users/register \
   -H "Content-Type: application/json" \
   -d '{
     "username": "adminuser",
     "email": "admin@example.com",
     "password": "SecurePass123"
   }'
```

- Manually assign the ADMIN role to your new account:

```
INSERT INTO user_roles (user_id, role_id)
VALUES (
  (SELECT id FROM users WHERE email = 'admin@example.com'),
  (SELECT id FROM roles WHERE role_name = 'ADMIN')
);
```

- Login with your new user to obtain a JWT with Admin rights:

```
curl -X POST http://localhost:8080/api/users/login \
-H "Content-Type: application/json" \
-d '{
  "email": "admin@example.com",
  "password": "SecurePass123"
}'

```


🧩 Summary

```
+-----------------------------------------------------------------------------+
|Step  |Action                               |Purpose                         |
|------|-------------------------------------|--------------------------------|
|1️     |init.sql auto-creates schema & roles |Database bootstrapping          |
|2️     |Register new user via /register      |Generates a valid BCrypt hash   |
|3️     |Assign ADMIN role manually           |Grants administrative access    |
|4️     |Use Swagger or Postman               |Explore all endpoints securely  |
+-----------------------------------------------------------------------------+
```

💡 Tip: After you’ve created and assigned your admin user, all subsequent user and role management can be done securely via the API and JWT authentication.



✅ You’re Done!

Your complete User Management System is now running fully in Docker.
Use Swagger UI or Postman to explore all available endpoints and verify RBAC functionality.


## 🧠 Explanations

---

### **1️⃣ Code Design Explanation**

The project follows a **modular and layered architecture** to ensure scalability, maintainability, and clean separation of concerns.

#### **Layered Architecture Overview**
- **Controller Layer:**  
  Handles all incoming REST API requests and responses. Each controller is annotated with `@RestController` and routes requests to corresponding service methods. Controllers do not contain business logic — they only handle validation and response formatting.

- **Service Layer:**  
  Contains the core business logic. Services perform validation, handle authorization checks, and interact with the repository layer. All external integrations (Kafka, caching, audit logging) are triggered from here.

- **Repository Layer:**  
  Responsible for data persistence and retrieval using **Spring Data JPA**. Each repository interface extends `JpaRepository` and automatically provides CRUD operations for entities.

- **Security Layer:**  
  Implements **JWT-based authentication** and **role-based access control (RBAC)** using `Spring Security`.  
  - `JwtAuthFilter` intercepts requests to validate JWT tokens.  
  - `CustomUserDetailsService` loads user details from the database.  
  - `SecurityConfig` defines route-level access permissions with `@PreAuthorize`.

- **Event Layer (Kafka Integration):**  
  Key events such as **User Registration** and **User Login** are published asynchronously to Kafka.  
  - `KafkaProducerService` serializes and publishes messages.  
  - `KafkaConsumerService` listens and logs the received events.  
  This design decouples event handling from business logic and supports scalability.

- **Audit Logging:**  
  The `AuditService` captures and stores key system actions (registrations, logins, role assignments) both in logs and in the database (`audit_log` table).

- **Caching:**  
  Frequently accessed endpoints such as `/api/users/me` use **Spring Cache** to reduce repetitive database queries and improve response performance.

- **Exception Handling:**  
  A centralized `GlobalExceptionHandler` handles all exceptions, returning structured JSON responses (`ApiResponse`) with consistent formatting and appropriate HTTP status codes.

---

### **2️⃣ Execution Explanation**

The execution flow demonstrates the **end-to-end lifecycle** of a user interacting with the API under a stateless JWT-based security model.

#### **Registration Flow**
1. Client sends a `POST /api/users/register` request with username, email, and password.  
2. `UserController` forwards it to `UserService`.  
3. Password is validated and securely hashed using **BCryptPasswordEncoder**.  
4. The user entity is persisted in MySQL.  
5. A “User Registered” event is published to Kafka.  
6. The service returns a JSON response confirming successful registration.

#### **Login Flow**
1. Client sends `POST /api/users/login` with email and password.  
2. Credentials are verified using `AuthenticationManager`.  
3. Upon success, a **JWT token** is generated by `JwtUtil`.  
4. A “User Logged In” event is published to Kafka.  
5. The token is returned to the client for subsequent authenticated requests.

#### **Profile Fetch Flow**
1. Authenticated client calls `GET /api/users/me` with JWT in the Authorization header.  
2. The token is validated by `JwtAuthFilter`.  
3. The authenticated user’s profile is fetched from cache or database.  
4. Response includes user ID, username, email, and assigned roles.

#### **Role Assignment Flow (Admin Only)**
1. Admin calls `POST /api/users/{userId}/roles` with JWT token.  
2. `@PreAuthorize("hasRole('ADMIN')")` ensures only admins can access.  
3. `RoleService` links the specified roles to the user.  
4. An audit entry and Kafka event are recorded.  
5. Returns a success message indicating roles were assigned.

#### **Role Creation Flow (Admin Only)**
1. Admin sends a `POST /api/roles` request with the role name in the request body.  
2. `RoleController` forwards the request to `RoleService`.  
3. The service validates that the role does not already exist.  
4. The new role is saved in the `roles` table through JPA.  
5. An audit log and Kafka event are created to record the action.  
6. A success response is returned confirming the role creation.

#### **Event Flow**
- Kafka events for login/registration are logged by the `KafkaConsumerService`.  
- This ensures traceability and provides asynchronous logging for external system integrations.

---

### **3️⃣ Result Output Explanation**

Each endpoint in the application returns **structured JSON responses** wrapped in a consistent format defined by `ApiResponse`.  
This ensures predictable client-side parsing and unified logging.

---

#### **✅ User Registration (`POST /api/users/register`)**
Input cURL: 

```
curl --location 'http://localhost:8080/api/users/register' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "test2",
  "email": "test2@example.com",
  "password": "test123"
}'
```

Output:

```
{
    "status": "success",
    "message": "User registered successfully",
    "data": {
        "id": 3,
        "username": "test2",
        "email": "test2@example.com"
    },
    "timestamp": "2025-10-22T20:13:58.063557167"
}
```


#### **✅ User Login (`POST /api/users/login`)**

Input cURL:

```
curl --location 'http://localhost:8080/api/users/login' \
--header 'Content-Type: application/json' \
--data-raw '{
  "email": "test2@example.com",
  "password": "test123"
}
'
```
Output:

```
{
    "status": "success",
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIkFETUlOIl0sImlhdCI6MTc2MTE2NDI3NiwiZXhwIjoxNzYxMTY3ODc2fQ.p9f5REgmOD3sw-1cyk_qP12t1CpQfwJPjs42a4TWSFM"
    },
    "timestamp": "2025-10-22T20:14:59.768812556"
}
```

#### **✅ Fetch Current User (`GET /api/users/me`)**

Input Curl:

```
curl --location 'http://localhost:8080/api/users/me' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkBleGFtcGxlLmNvbSIsInJvbGVzIjpbXSwiaWF0IjoxNzYxMTY0MDk5LCJleHAiOjE3NjExNjc2OTl9.ws9rbEH1Ije0VanwXgsJt920LxMSmkJEnPfQG1oEi2s'
```
Output:

```
{
    "status": "success",
    "message": "User profile retrieved successfully",
    "data": {
        "username": "test2",
        "roles": [ADMIN],
        "email": "test2@example.com",
        "id": 3
    },
    "timestamp": "2025-10-22T20:18:18.16095747"
}
```

#### **✅ Create Role (`POST /api/roles`)**

Input cURL:

```
curl --location --request POST 'http://localhost:8080/api/roles?roleName=TEST' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIkFETUlOIl0sImlhdCI6MTc2MTE2NDI3NiwiZXhwIjoxNzYxMTY3ODc2fQ.p9f5REgmOD3sw-1cyk_qP12t1CpQfwJPjs42a4TWSFM'
```

Output:

```
{
    "status": "success",
    "message": "Role created successfully",
    "data": {
        "id": 2,
        "name": "TEST"
    },
    "timestamp": "2025-10-22T20:18:24.926064253"
}
```

#### **✅ Assign Role to User (`POST /api/users/{userId}/roles`)**

Input cURL:

```
curl --location --request POST 'http://localhost:8080/api/users/3/roles?roleNames=MANAGER&roleNames=TEST' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIkFETUlOIl0sImlhdCI6MTc2MTE2NDI3NiwiZXhwIjoxNzYxMTY3ODc2fQ.p9f5REgmOD3sw-1cyk_qP12t1CpQfwJPjs42a4TWSFM'
```

Output:

```
{
    "status": "success",
    "message": "Roles assigned successfully",
    "data": [
        {
            "userId": 3,
            "username": "test2",
            "assignedRole": "MANAGER"
        },
        {
            "userId": 3,
            "username": "test2",
            "assignedRole": "TEST"
        },
        {
            "userId": 3,
            "username": "test2",
            "assignedRole": "ADMIN"
        }
    ],
    "timestamp": "2025-10-22T20:20:38.091404024"
}
```

#### **✅ Get Admin Stats (`GET /api/admin/stats`)**

Input cURL:

```
curl --location 'http://localhost:8080/api/admin/stats' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIkFETUlOIl0sImlhdCI6MTc2MTE2NDI3NiwiZXhwIjoxNzYxMTY3ODc2fQ.p9f5REgmOD3sw-1cyk_qP12t1CpQfwJPjs42a4TWSFM'
```

```
{
    "status": "success",
    "message": "System statistics retrieved",
    "data": {
        "totalUsers": 3,
        "lastLogins": [
            {
                "username": "test1",
                "lastLogin": "2025-10-22T20:09:40"
            },
            {
                "username": "test4",
                "lastLogin": null
            },
            {
                "username": "test2",
                "lastLogin": "2025-10-22T20:17:56"
            }
        ]
    },
    "timestamp": "2025-10-22T20:21:33.380199643"
}

```

#### **⚠️ Error Response Example**
1. Registration Error 

```
{
    "status": "failure",
    "message": "Email already exists.",
    "data": null,
    "timestamp": "2025-10-22T20:22:06.544775218"
}
```

2. Login Error:

```
{
    "status": "failure",
    "message": "Invalid email or password.",
    "data": null,
    "timestamp": "2025-10-22T20:22:26.550950361"
}

```


### **4️⃣ Testing Strategy Explanation**

The backend follows a **test-driven and modular testing strategy** to ensure that all layers of the application — from services to controllers — behave as expected under various conditions.  
The goal of the testing framework is to validate both **individual logic units** and **end-to-end request flows**, with clear separation between **unit** and **integration** tests.

Test Directory Structure:

```
src
└── test
    └── java
        ├── com.visitly.test.unit
        │   ├── controller
        │   │   ├── AdminControllerTest.java
        │   │   ├── RoleControllerTest.java
        │   │   └── UserControllerTest.java
        │   └── service
        │       ├── RoleServiceTest.java
        │       └── UserServiceTest.java
        └── com.visitly.test.integration
            └── controller
                ├── AdminControllerIntegrationTest.java
                ├── RoleControllerIntegrationTest.java
                └── UserControllerIntegrationTest.java
```


#### **🧩 Unit Testing**

Unit tests focus on verifying core business logic in isolation, mocking out all external dependencies such as repositories, Kafka producers, and mappers.  
This ensures that service logic, validation rules, and audit logging work correctly without requiring a live database or Kafka broker.

**Key Areas Covered:**
- `UserServiceTest` → Tests user registration, login flow, password hashing, and Kafka event publishing.  
- `RoleServiceTest` → Verifies role creation, assignment, and audit logging.  
- `UserControllerTest`, `RoleControllerTest`, `AdminControllerTest` → Validate controller-level logic using mocked service layers.  

**Frameworks & Tools:**
- **JUnit 5** for test execution and assertions.  
- **Mockito** for mocking dependencies and verifying interactions.  
- **Log4j2 Test Logging** to ensure audit and event logs are produced as expected.  
- `@ExtendWith(MockitoExtension.class)` to manage the lifecycle of mocks cleanly.



#### **🌐 Integration Testing**

Integration tests validate the behavior of the REST API endpoints as they would function in a real deployment.  
These tests check request mappings, JSON serialization, authentication, and security enforcement using Spring Boot’s test utilities.

**Key Areas Covered:**

- `UserControllerIntegrationTest` → Tests registration, login, JWT authentication, and profile retrieval endpoints |
- `RoleControllerIntegrationTest` → Validates role creation and assignment under authenticated ADMIN context |
- `AdminControllerIntegrationTest` → Ensures that admin-only endpoints are secured and return system statistics correctly |

**Frameworks & Tools:**
- `@WebMvcTest` + **MockMvc** for endpoint simulation.  
- `@WithMockUser` for role-based access testing.  
- **Spring Security Test** utilities to verify authentication and authorization logic.  
- **Jackson ObjectMapper** for request/response serialization.

#### **🧰 Running the Tests**

**Run All Tests** (Local)

```
./mvnw test
```

**Run a Specific Test** (Local)

```
./mvnw -Dtest=UserServiceTest test
```

## 🖼️ API Response Gallery

Below are sample screenshots demonstrating successful and failure scenarios across major endpoints.

### 🧍‍♂️ User Registration
**✅ Success**

![Register Success](./assets/register_success_image.jpg)

**❌ Failure (Duplicate Email)**

![Registration Failure](./assets/registration_failure_image.jpg)

---

### 🔐 User Login
**✅ Success**

![Login Success](./assets/login_success_image.jpg)

**❌ Failure (Invalid Credentials)**

![Login Failure](./assets/login_failure_image.jpg)

---

### 👤 Current User Profile Fetch
**✅ Success**

![Current User Success](./assets/current_user_success_image.jpg)

