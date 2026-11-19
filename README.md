# Bug & Issue Tracker Backend (Jira-Like REST API)

A production-ready modular monolithic backend built with **Java 21**, **Spring Boot 3.4**, **Spring Security + JWT**, **Flyway**, and **PostgreSQL**.

## Tech Stack
- **Language & Framework:** Java 21, Spring Boot 3.4.1
- **Security:** Spring Security 6, Stateless JWT Access Tokens + Rotating Refresh Tokens (DB-backed revocation), BCrypt
- **Database & Migrations:** PostgreSQL, Spring Data JPA, Hibernate ORM, Flyway
- **Documentation & Monitoring:** SpringDoc OpenAPI / Swagger UI, Spring Boot Actuator
- **Testing:** JUnit 5, Mockito

## Local Setup (Windows 11 + PowerShell)

1. **Create PostgreSQL Database:**
   Open `psql` or pgAdmin and run:
   ```sql
   CREATE DATABASE bugtracker_db;
   ```

2. **Configure Environment Variables (Optional):**
   Copy `.env.exam` to `.env` or set environment variables in IntelliJ Run Configuration:
   - `DB_URL=jdbc:postgresql://localhost:5432/bugtracker_db`
   - `DB_USERNAME=postgres`
   - `DB_PASSWORD=your_password`

3. **Build & Run:**
   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

4. **Explore Swagger API Docs:**
   Open `http://localhost:8080/swagger-ui.html` in your browser.
   - Note: The **very first user** registered via `POST /api/auth/register` is automatically assigned the `ADMIN` role. Subsequent public registrations receive `DEVELOPER`.

## Key REST Endpoints
- **Auth:** `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`, `GET /api/auth/me`
- **Users:** `POST /api/users`, `GET /api/users`, `GET /api/users/{id}`, `PUT /api/users/{id}`, `PATCH /api/users/{id}/role`, `DELETE /api/users/{id}`
- **Projects:** `POST /api/projects`, `GET /api/projects`, `GET /api/projects/{id}`, `PUT /api/projects/{id}`, `DELETE /api/projects/{id}`, `POST /api/projects/{id}/members`, `DELETE /api/projects/{id}/members/{userId}`, `GET /api/projects/{id}/members`
- **Issues:** `POST /api/issues`, `GET /api/issues` (supports `projectId`, `status`, `priority`, `type`, `assigneeId`, `search`, `page`, `size`, `sortBy`, `sortDir`), `GET /api/issues/{id}`, `PUT /api/issues/{id}`, `PATCH /api/issues/{id}/status`, `PATCH /api/issues/{id}/assignee`, `DELETE /api/issues/{id}`
- **Kanban Board:** `GET /api/projects/{projectId}/board`
- **Comments:** `POST /api/issues/{issueId}/comments`, `GET /api/issues/{issueId}/comments`, `PUT /api/comments/{commentId}`, `DELETE /api/comments/{commentId}`
- **Attachments:** `POST /api/issues/{issueId}/attachments`, `GET /api/issues/{issueId}/attachments`, `GET /api/attachments/{attachmentId}/download`
