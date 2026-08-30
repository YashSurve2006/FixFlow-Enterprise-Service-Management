# FixFlow - Viva Preparation Notes

This document contains key speaking points, architectural defenses, and technical details to assist you during your final presentation/viva for the FixFlow project.

## 1. Why Java 21 & Jakarta REST (No Spring Boot)?
- **Defense:** Building natively with JAX-RS (Jersey) demonstrates a profound understanding of core HTTP, REST boundaries, and component lifecycles without relying on the "black-box" magic of Spring Boot.
- **Benefit:** It guarantees a smaller memory footprint on Apache Tomcat, tighter control over connection pooling, and deep knowledge of servlet mappings (via `@ApplicationPath`).

## 2. Security: JWT & BCrypt
- **JWT (JSON Web Token):** We use stateless tokens issued during login. The token payload contains the user's ID and Role. It is signed with a server-side secret using `HS256`. This prevents session-hijacking and eliminates the need for Tomcat to store `HttpSession` memory, making the server perfectly scalable.
- **BCrypt:** Passwords are mathematically hashed with a salt (`$2a$`). It is physically impossible to reverse-engineer the original password even if the database is breached.
- **Custom Authentication Filter:** A `ContainerRequestFilter` intercepts every incoming API call. If a Java method has the `@Secured` annotation, the filter validates the JWT signature and blocks unauthorized roles (`403 Forbidden`) natively at the routing layer.

## 3. Database: JDBC & Connection pooling
- **Defense:** Instead of using heavy ORMs like Hibernate, we use raw `java.sql.PreparedStatement` and manual result-mapping.
- **Security:** Using `?` parameters completely negates SQL Injection vulnerabilities because the database driver treats the parameters strictly as data, never executable SQL.
- **Transactions:** Complex actions (like assigning a technician) require atomic guarantees. We use `connection.setAutoCommit(false)` to lock the scope, execute both the assignment insert and status update, and then `connection.commit()`. If an error occurs, `connection.rollback()` fires instantly, preventing orphan records.

## 4. REST API Design (RESTful maturity)
- We strictly separate HTTP Methods by purpose:
  - `GET` - Idempotent resource retrieval (e.g., fetching categories).
  - `POST` - Creating new resources (e.g., submitting a request).
  - `PATCH` - Partially modifying a resource (e.g., updating a status).
  - `DELETE` - Removing resources.
- **Pagination & Filtering:** Exposed dynamically through Query Parameters (`?status=PENDING&page=1&limit=10`). 
- **Envelopes:** Exception Mappers catch all backend errors (like `SQLException`) and serialize them into standard `{"status": 500, "message": "..."}` JSON envelopes to prevent stack traces from bleeding to the frontend.

## 5. Frontend: Vanilla JS & Fetch API
- **No React/Angular:** The frontend is built with pure ES6 JavaScript. 
- **CSS Architecture:** Uses CSS Variables for global state management. `[data-theme="dark"]` modifies the variables dynamically, triggering the entire application to swap themes without flashing or redundant CSS files.
- **API Connectivity:** The global `api.js` interceptor wraps the native `fetch()` browser API. It automatically attaches the `Authorization: Bearer <token>` header to all outgoing requests and traps `401 Unauthorized` responses to redirect users to the login screen immediately.
