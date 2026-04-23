# Library Management - Spring Boot Author and Book Management

This project is a Spring Boot REST API for managing authors and books, with JWT-based authentication and role-based authorization.

It includes:
- CRUD APIs for `Author` and `Book`
- Authentication APIs (`register`, `login`) with JWT
- Role-based endpoint protection (`ADMIN`, `LIBRARIAN`, `USER`)
- CSV import support for authors and books
- Swagger/OpenAPI documentation
- Service/repository/security test suites
- Docker and Jenkins setup files

---

## 1) Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+ (or use `mvnw.cmd`)
- PostgreSQL 15+ (or Docker)

### Run locally (Windows PowerShell)
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
.\mvnw.cmd spring-boot:run
```

### Build JAR
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
.\mvnw.cmd clean package
```

### Run packaged JAR
```powershell
java -jar .\target\crud-0.0.1-SNAPSHOT.jar
```

### Run with Docker Compose
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
docker compose up --build
```

---

## 2) Project Flow (End to End)

### A) Startup Flow
1. `CrudApplication` starts Spring Boot.
2. Spring loads configuration from `application.properties`.
3. `SecurityConfig` creates security filter chain.
4. `SecurityConfig` `CommandLineRunner` ensures default admin exists (`admin` / `admin123`) if missing.
5. App starts on port `8081` by default.

### B) Authentication Flow (`/auth/*`)
1. Client calls `POST /auth/register` with username/password/role.
2. `AuthController` validates input, hashes password, stores user in DB.
3. Client calls `POST /auth/login` with credentials JSON.
4. `AuthController` verifies password using `PasswordEncoder`.
5. `JwtUtil` generates signed JWT token with `subject=username`, `claim=role`, `exp=1h`.

### C) Authorized API Flow (`/api/*`)
1. Client sends `Authorization: Bearer <token>`.
2. `JwtFilter` validates token and loads user via `CustomerUserDetailsService`.
3. Spring Security creates authentication context with role authority (`ROLE_ADMIN`, etc.).
4. Request reaches controller only if role matches route policy.
5. Controller delegates to service -> service uses repository -> repository talks to database.

### D) Author CRUD Flow
- `AuthorController` accepts requests and validates DTOs.
- `AuthorService` maps DTO <-> entity, applies business checks.
- `AuthorRepository` performs DB operations.
- `GlobalExceptionHandler` converts domain exceptions to clean HTTP responses.

### E) Book CRUD Flow
- Same layered flow as author.
- Additional rule: `BookService` verifies referenced author exists before create/update/import.

### F) CSV Import Flow
1. Client sends `multipart/form-data` with a file field named `file`.
2. `CSVReader` parses rows to DTO list.
3. Service methods iterate DTO list and save entities.

---

## 3) Security and Authorization Matrix

Configured in `src/main/java/com/authorbooksystem/crud/security/SecurityConfig.java`.

- Public:
  - `/auth/**`
  - `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `/api-docs/**`
- Protected:
  - `GET /api/books/**`, `GET /api/authors/**` -> `ADMIN` or `LIBRARIAN`
  - `POST /api/books/**`, `POST /api/authors/**` -> `ADMIN`
  - `PUT /api/books/**`, `PUT /api/authors/**` -> `ADMIN`
  - `DELETE /api/books/**`, `DELETE /api/authors/**` -> `ADMIN`

Important notes:
- JWT is stateless; server session is disabled.
- Invalid token does not crash app; request continues unauthenticated and is rejected by security rules.

---

## 4) API Reference (Current Routes)

Base URL: `http://localhost:8081`

### Auth
- `POST /auth/register`
- `POST /auth/login`

Example request bodies:

```json
{
  "username": "admin2",
  "password": "password123",
  "role": "ADMIN"
}
```

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Authors
- `POST /api/authors`
- `POST /api/authors/import` (multipart CSV)
- `GET /api/authors`
- `PUT /api/authors/{id}`
- `DELETE /api/authors/{id}`

### Books
- `POST /api/books`
- `POST /api/books/import` (multipart CSV)
- `GET /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

### Swagger
- UI: `http://localhost:8081/swagger-ui.html`
- Docs JSON: `http://localhost:8081/api-docs`

---

## 5) CSV Format Requirements

### Author CSV
Expected header:
```text
name,email
```
Example row:
```text
John Doe,john@example.com
```

### Book CSV
Expected header:
```text
title,genre,authorId
```
Example row:
```text
Clean Code,Programming,1
```

Notes:
- CSV parser currently uses simple `split(",")`; quoted commas are not handled.
- Upload endpoints expect form key `file`.

---

## 6) Folder and File-by-File Walkthrough

### Root folder: `SpringBoot_Author_Book_Management/`

- `.gitattributes` - Git attributes configuration.
- `.gitignore` - Ignored files/folders for Git (build outputs, IDE files, etc.).
- `.mvn/` - Maven Wrapper internals.
- `mvnw`, `mvnw.cmd` - Maven Wrapper scripts for Unix/Windows.
- `pom.xml` - Main build descriptor: dependencies, plugins, Java version, packaging.
- `Dockerfile` - Container image recipe; runs packaged JAR on port 8081.
- `docker-compose.yml` - Multi-container setup for app + PostgreSQL.
- `test-jwt.ps1` - PowerShell script to test auth/JWT flow quickly.
- `test-jwt.sh` - Bash version of JWT test script.
- `README.md` - This documentation file.

Documentation and change logs:
- `aop-logging-implementation.md` - Notes about AOP logging setup and rationale.
- `JWT-Security-Fix-Summary.md` - Security/JWT fix summary and behavior notes.
- `POST-Books-403-Solution-Guide.md` - Troubleshooting guide for 403 on protected endpoints.
- `Service-Testing-Enhancement-Summary.md` - Service test expansion summary.
- `swagger-errors-fix.md` - Swagger troubleshooting notes.
- `TESTING-DOCUMENTATION.md` - broad testing strategy write-up.
- `TESTING-STATUS-REPORT.md` - status snapshot of test execution at that point in time.
- `UserRepositoryTest-Enhancement-Summary.md` - details about repository test improvements.

Folders:
- `Jenkins/` - CI pipeline definitions.
- `src/` - application source and test code.
- `target/` - generated build output (compiled classes, jars).

### `Jenkins/`
- `Jenkinsfile` - pipeline with stages: checkout, build, test, archive artifact.

### `src/main/java/com/authorbooksystem/crud/`

#### App bootstrap
- `CrudApplication.java`
  - Main entry class with `@SpringBootApplication`.

#### `aspect/`
- `LoggingAspect.java`
  - AOP logging around service methods.
  - Logs method entry, successful exit, and exceptions.

#### `config/`
- `OpenApiConfig.java`
  - OpenAPI bean with title, description, version.

#### `controller/`
- `AuthController.java`
  - `POST /auth/register`: validates and registers users.
  - `POST /auth/login`: checks credentials and returns JWT.
- `AuthorController.java`
  - Author CRUD endpoints and CSV import endpoint.
  - Includes Swagger annotations and validation.
- `BookController.java`
  - Book CRUD endpoints and CSV import endpoint.
  - Includes Swagger annotations and validation.

#### `dto/request/`
- `AuthorRequestDTO.java` - input DTO for author create/update, with validation.
- `BookRequestDTO.java` - input DTO for book create/update, with validation.
- `LoginDTO.java` - login request payload (`username`, `password`).
- `RegisterRequest.java` - registration payload with validation constraints.

#### `dto/response/`
- `AuthorResponseDTO.java` - API response shape for author.
- `BookResponseDTO.java` - API response shape for book.

#### `entity/`
- `Author.java` - JPA entity for authors; one-to-many relation to books.
- `Book.java` - JPA entity for books; many-to-one relation to author.
- `User.java` - JPA entity for security users (`username`, hashed `password`, `role`).

#### `exception/`
- `AuthorNotFoundException.java` - custom runtime exception.
- `BookNotFoundException.java` - custom runtime exception.
- `GlobalExceptionHandler.java` - central exception-to-response mapping.

#### `repository/`
- `AuthorRepository.java` - JPA repository for author entity.
- `BookRepository.java` - JPA repository for book entity.
- `UserRepository.java` - JPA repository for user entity + `findByUsername`.

#### `security/`
- `SecurityConfig.java`
  - Security filter chain, route authorization rules, stateless session config.
  - Password encoder bean.
  - Startup admin seeding logic.
- `JwtFilter.java`
  - Extracts Bearer token, validates JWT, loads user details.
- `JwtUtil.java`
  - Generates and validates JWT tokens.
- `CustomerUserDetailsService.java`
  - Fetches users from DB and returns `UserDetails`.
- `UserPrincipal.java`
  - Adapts `User` entity to Spring Security `UserDetails` contract.

#### `service/`
- `AuthorService.java`
  - Author business logic for create/read/update/delete.
  - CSV bulk create and CSV export helper method.
- `BookService.java`
  - Book business logic with author existence checks.
  - CSV bulk create and CSV export helper method.

#### `utils/`
- `CSVReader.java`
  - Parses uploaded CSV files into DTO lists.
- `ToCsv.java`
  - Converts entity lists into CSV text.

### `src/main/resources/`
- `application.properties`
  - Datasource settings (env override capable), JPA mode, port, Swagger paths.

### `src/test/java/com/authorbooksystem/crud/`
- `CrudApplicationTests.java` - context load smoke test.
- `CrudApplicationIntegrationTest.java` - integration-style tests for auth/security behavior.

#### `repository/`
- `UserRepositoryTest.java`
  - Extensive repository behavior tests using `@DataJpaTest` and H2.

#### `security/`
- `CustomerUserDetailsServiceTest.java`
- `JwtUtilTest.java`
- `UserPrincipalTest.java`
  - Unit tests for security components and token utility behavior.

#### `service/`
- `AuthorServiceTest.java`
- `BookServiceTest.java`
  - Mockito-based service layer tests for business logic and edge cases.

### `src/test/resources/`
- `application-test.properties`
  - Test datasource and test logging config.

### `target/`
Generated by Maven build:
- compiled classes
- packaged jar files (`crud-0.0.1-SNAPSHOT.jar`)
- surefire reports
- generated sources

Do not manually edit `target/`; rebuild instead.

---

## 7) Build and Test Commands

### Run full test suite
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
.\mvnw.cmd test
```

### Build without tests
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
.\mvnw.cmd -DskipTests clean package
```

### Run specific test class
```powershell
cd "C:\Users\geetanjalipandey\OneDrive - Nagarro\Desktop\library management\SpringBoot_Author_Book_Management"
.\mvnw.cmd -Dtest=UserRepositoryTest test
```

---

## 8) Configuration Cheat Sheet

From `application.properties`:
- `spring.datasource.url=${SPRING_DATABASE_URL:jdbc:postgresql://localhost:5432/Library}`
- `spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}`
- `spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}`
- `spring.jpa.hibernate.ddl-auto=update`
- `server.port=${SERVER_PORT:8081}`
- `springdoc.swagger-ui.path=/swagger-ui.html`
- `springdoc.api-docs.path=/api-docs`

Quick env override example (PowerShell):
```powershell
$env:SPRING_DATABASE_URL="jdbc:postgresql://localhost:5432/Library"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="root"
.\mvnw.cmd spring-boot:run
```

---

## 9) CI/CD and Deployment Notes

### Jenkins
- Pipeline file: `Jenkins/Jenkinsfile`
- Typical stages:
  - Checkout
  - Build (package)
  - Test
  - Archive JAR artifact from `target/`

### Docker
- `Dockerfile` expects JAR at `target/crud-0.0.1-SNAPSHOT.jar`.
- Build JAR before building image if using plain `docker build .`.

### Compose
- Brings up app and PostgreSQL together.
- Uses environment variables for datasource URL and credentials.

---

## 10) Revision Notes (Interview/Exam Friendly)

### Layering and responsibilities
- Controller = HTTP handling + validation trigger.
- Service = business logic and orchestration.
- Repository = persistence abstraction.
- DTOs decouple API contract from DB entities.

### Security essentials in this project
- JWT token is generated on login and validated per request.
- `UserPrincipal` maps DB role to Spring authority using `ROLE_` prefix.
- Authorization is method and route based via `HttpMethod` matchers.
- Password is stored hashed via `BCryptPasswordEncoder`.

### JPA model essentials
- `Author` <-> `Book` relationship is one-to-many / many-to-one.
- `Book.author_id` is join column to author primary key.
- `User` stored in `users` table.

### Validation essentials
- DTO validation annotations enforce input constraints.
- `@Valid` in controllers triggers automatic validation.
- `GlobalExceptionHandler` formats validation errors as HTTP 400 responses.

### Testing essentials
- Unit tests for service/security use Mockito and AssertJ.
- Repository tests use `@DataJpaTest` with H2.
- Integration tests validate context + core auth/security behavior.

### Known implementation caveats (good for revision)
- `JwtUtil` secret is hardcoded; production should externalize and rotate secrets.
- CSV parsing uses naive comma split; production-grade parser should handle quoted fields.
- Some project docs may reflect earlier states; source code is the final truth.

---

## 11) Suggested Next Improvements

1. Move JWT secret to environment variable and add key-length/rotation policy.
2. Replace manual CSV parser with Apache Commons CSV or OpenCSV.
3. Add unique constraints for usernames and author email where needed.
4. Standardize exception types/messages in all service methods.
5. Add endpoint/versioned API docs section with sample request/response payloads.
6. Add Jacoco and quality gates in Jenkins.

---

## 12) Useful Links During Local Run

- App base: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`

If you are blocked by 403 on protected APIs, first login using an `ADMIN` user and attach `Bearer` token in `Authorization` header.

