# FixFlow Architecture

## System Overview
FixFlow is a premium Enterprise Service Management platform built on a monolithic Java backend utilizing Jakarta REST and a Vanilla HTML5/JS frontend.

## Data Flow
```mermaid
graph TD
    A[Browser / Frontend] -->|Fetch API (HTTP)| B[Tomcat Server]
    B -->|JAX-RS / Jersey| C[REST Resources]
    C -->|Authentication Filter| D[Service Layer]
    D -->|DAO Layer| E[JDBC Driver]
    E -->|SQL| F[(MySQL 8 Database)]
    F -->|Result Sets| E
    E -->|Java Objects| D
    D -->|Business Logic| C
    C -->|JSON| B
    B -->|HTTP Response| A
```

## Backend Architecture
The backend is strictly built on Java 21 without Spring Boot. It uses:
- **Jakarta REST (JAX-RS):** Exposing robust `GET`, `POST`, `PUT`, `PATCH`, `DELETE` endpoints.
- **Jersey:** The JAX-RS implementation providing the REST engine.
- **JDBC / Connection Pooling:** Custom `DatabaseUtil` manages pure SQL connections via `setenv` configuration.
- **Layered Pattern:** Code is strictly segregated into `Resource` -> `Service` -> `DAO` -> `Model`.

## Security Architecture
- **JWT (JSON Web Tokens):** The `AuthResource` issues a signed JWT upon successful login.
- **BCrypt:** Passwords are mathematically hashed using `jbcrypt` (salt revision `$2a$`).
- **RBAC (Role-Based Access Control):** Utilizing `@Secured` annotations, a custom `AuthenticationFilter` intercepts requests to validate token signatures and inspect the `SecurityContext` for `USER`, `TECHNICIAN`, or `ADMIN` roles.

## Frontend Architecture
The frontend intentionally avoids heavy JavaScript frameworks like React or Angular to minimize dependency drift and load times.
- **HTML5 & CSS3 Variables:** Utilizing CSS Custom Properties (e.g., `--bg-surface`, `--primary-color`) enables a seamless dynamic Light/Dark mode swap via `[data-theme="dark"]`.
- **Vanilla JavaScript & Fetch API:** A central `api.js` layer handles all API interactions, injecting JWT headers into requests and intercepting global 401s to force redirects.
- **Component-based CSS:** Styles are logically split across `global.css`, `components.css`, `dashboard.css`, `auth.css`, and `responsive.css` to prevent conflicts and maintain an enterprise aesthetic.

## Request Lifecycle (State Machine)
Every Service Request follows a strict workflow:
1. `PENDING`: Request created by `USER`.
2. `ASSIGNED`: Request delegated to a `TECHNICIAN` by an `ADMIN`.
3. `IN_PROGRESS`: `TECHNICIAN` acknowledges and begins work.
4. `RESOLVED`: `TECHNICIAN` marks the issue as structurally fixed.
5. `CLOSED`: `ADMIN` archives the request permanently.

## Error & Transaction Handling
- **Transactions:** `DAO` layer manually invokes `connection.setAutoCommit(false)` and `connection.commit()` to ensure atomic data insertion (e.g., creating a request and assignment relation simultaneously).
- **Global Error Maps:** Exception Mappers convert Java Exceptions (`IllegalArgumentException`, `SQLException`) into uniform JSON error envelopes `{ "status": 400, "message": "..." }` preventing stack trace leakage.
