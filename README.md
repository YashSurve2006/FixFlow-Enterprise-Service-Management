# FixFlow – Smart Service Request & Maintenance Management System

FixFlow helps users report and track maintenance and service-related issues. Users can create, view, update, partially modify, and delete service requests. Administrators and technicians can manage, assign, update, and resolve requests.

## Technology Stack
- **Java 21**
- **Maven**
- **Jakarta REST / JAX-RS (Jersey 3.1.8)**
- **Apache Tomcat (v10+)**
- **MySQL (v9.0.0)**
- **JDBC**
- **Security:** JJWT (Java JWT) & jBCrypt

## Authentication Approach
The application relies on JSON Web Token (JWT) based authentication and BCrypt password hashing.
1. The user registers a new account (assigned `USER` role automatically).
2. The user logs in to receive a JWT `Bearer` token.
3. The token is sent in the `Authorization: Bearer <token>` header on protected endpoints.
4. An `AuthenticationFilter` intercepts requests, validates the JWT using the secret key, extracts the User ID and Role, and populates a custom `SecurityContext`.
5. The `ServiceRequestResource` and `UserResource` are protected using a custom `@Secured` annotation to enforce role-based access control (RBAC).

## Supported Roles
- **USER:** Can create service requests, and view/modify/delete only their *own* requests.
- **TECHNICIAN:** Can view assigned requests. Once an Admin assigns a request to them, they can transition the status from `ASSIGNED` -> `IN_PROGRESS` -> `RESOLVED`.
- **ADMIN:** Can view and manage all users, categories, service requests, and technician assignments across the platform.

## API Endpoints & Features

### Core API Format
In Stage 5, all Collection endpoints (e.g. `GET /api/requests`) return a `PaginatedResponse` structure containing `data` and `pagination` objects. Single-entity endpoints return a `DataResponse` containing a `data` wrapper to maintain API consistency.

### Public Endpoints
- `GET /api/health` - Basic health check
- `POST /api/auth/register` - Register a new `USER`
- `POST /api/auth/login` - Authenticate and retrieve a JWT

### Protected Endpoints (Requires `Authorization: Bearer <token>`)
- **Users API (`/api/users`)**: 
  - `GET /{id}` (Self or ADMIN)
  - `GET`, `POST`, `PUT`, `PATCH`, `DELETE` (ADMIN only)
- **Service Requests API (`/api/requests`)**:
  - `GET` (Supports Pagination, Search, and Filtering. USER sees own; ADMIN/TECHNICIAN sees allowed)
  - `GET /{id}` (USER sees own; ADMIN/TECHNICIAN sees allowed)
  - `POST` (USER and ADMIN only)
  - `PUT`, `PATCH`, `DELETE /{id}` (USER can modify own; ADMIN can modify all)
  - `PATCH /{id}/status` (Updates workflow status based on State Machine Rules)
  - `GET`, `POST /{id}/assignment` (Fetch or assign a technician to a request)
  - `GET /statistics` (ADMIN only. Aggregated database metrics)
- **Assignments API (`/api/assignments`)**:
  - `GET`, `GET /{id}`, `PUT`, `DELETE` (ADMIN driven, TECHNICIAN can view own)
- **Categories API (`/api/categories`)**:
  - `GET`, `GET /{id}` (Viewable by authenticated users)
  - `POST`, `PUT`, `PATCH`, `DELETE` (ADMIN only)

## Filtering, Pagination, and Search
The `GET /api/requests` endpoint supports advanced SQL-driven querying:
- **Pagination**: `?page=1&limit=10`
- **Search**: `?search=leak` (Searches title, description, and location)
- **Filtering**: 
  - `?status=PENDING`
  - `?priority=URGENT`
  - `?categoryId=2`
  - `?fromDate=2026-01-01&toDate=2026-12-31`
- **Sorting**: `?sortBy=createdAt&sortOrder=desc`

## CORS Configuration
Cross-Origin Resource Sharing (CORS) is explicitly configured via a JAX-RS `ContainerResponseFilter` (`CorsFilter.java`).
- Allowed Origin: `http://localhost:3000`
- Allowed Methods: `GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH`
- Credentials: `true`

## Web Services Concepts Demonstrated
FixFlow is built as a pure Java EE/Jakarta Web Service demonstration project. Key concepts illustrated include:
- **REST Architecture**: Resource-based URLs (`/api/requests`, `/api/users`).
- **HTTP Methods**: Proper utilization of `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- **Statelessness**: REST APIs are entirely stateless. Sessions are managed strictly via **JWT Authentication**.
- **JSON Serialization**: Using Jakarta JSON-B/Jackson to seamlessly serialize DTOs to standard JSON formats.
- **HTTP Status Codes**: Meaningful returns (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `409 Conflict`).
- **CORS Handling**: Cross-Origin Resource Sharing handled natively using JAX-RS Filters.
- **Dynamic SQL Querying**: Implementing Server-side filtering, sorting, and pagination.

## Frontend Architecture (Vanilla JS)
The frontend completely detaches from the backend logic, running natively in the browser using:
- **HTML5/CSS3** (No UI frameworks like Bootstrap or Tailwind)
- **Vanilla JavaScript** (No React, Vue, or Angular)
- **JavaScript Fetch API**: Standardized asynchronous requests (`fetch()`) wrapping all backend interactions in `js/api.js`.

### Authentication Flow
1. User logs in via `POST /api/auth/login`.
2. Frontend receives a JWT and stores it in `localStorage`.
3. The `js/api.js` client automatically intercepts and injects `Authorization: Bearer <token>` into all subsequent `fetch` requests.
4. UI dashboards route users based on role (`ADMIN`, `TECHNICIAN`, `USER`). Server validates permissions independently.
FixFlow implements strict State Machine rules for service request workflows:
- `PENDING` -> `ASSIGNED` (Triggered automatically when an Admin assigns a technician)
- `ASSIGNED` -> `IN_PROGRESS` (Triggered by the assigned Technician)
- `IN_PROGRESS` -> `RESOLVED` (Triggered by the assigned Technician)
- `RESOLVED` -> `CLOSED` (Triggered by Admin)
- *Note: ADMINs can cancel (`CANCELLED`) requests from the PENDING stage if needed.*

## Environment Variables
Set the following globally on your machine or in your Tomcat Server Run Configuration:
- `DB_URL`: The JDBC URL (e.g. `jdbc:mysql://localhost:3306/fixflow_db`)
- `DB_USER`: Your MySQL username
- `DB_PASSWORD`: Your MySQL password
- `JWT_SECRET`: A secure, secret string used to sign JWT tokens.

## Database Setup
1. Create a local MySQL server instance.
2. Run the SQL script found at `src/main/resources/database/fixflow_schema.sql` to generate the `fixflow_db`, tables, and foreign keys.

## How to Run
1. Open a terminal and run `mvn clean package`.
2. Move the generated `target/FixFlow.war` into your Tomcat 10+ `webapps` folder, or configure your IDE to run a local Tomcat server pointing to this exploded artifact.
3. Start Tomcat. The application should be accessible at `http://localhost:8080/FixFlow/api/health`.

## How to Test Authentication with Postman

### 1. Register
**POST** `http://localhost:8080/FixFlow/api/auth/register`
**Headers:** `Content-Type: application/json`
**Body:**
```json
{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phone": "1234567890",
    "password": "mypassword123"
}
```

### 2. Login
**POST** `http://localhost:8080/FixFlow/api/auth/login`
**Headers:** `Content-Type: application/json`
**Body:**
```json
{
    "email": "jane@example.com",
    "password": "mypassword123"
}
```
*Note the `token` in the response.*

### 3. Authenticated Request (Create Service Request)
**POST** `http://localhost:8080/FixFlow/api/requests`
**Headers:** 
- `Content-Type: application/json`
- `Authorization: Bearer <PASTE_YOUR_TOKEN_HERE>`
**Body:**
```json
{
    "categoryId": 1,
    "title": "Leaking Pipe",
    "description": "Water is leaking in the kitchen.",
    "priority": "HIGH",
    "location": "Kitchen, Building A"
}
```
*(Notice that `userId` is not passed in the body; it is safely extracted from the token context).*

## Application Data Flow
This diagram illustrates the complete data flow from the browser to the database and back:

1. **Browser** triggers a user action (e.g., clicking Login).
2. **Fetch API** executes an asynchronous HTTP Request with JSON body.
3. **HTTP Request** hits the Tomcat server and is routed to the JAX-RS Resource.
4. **Authentication Filter** intercepts the request to validate the JWT (if required).
5. **Service Layer** processes the business logic.
6. **DAO Layer** translates the request into JDBC operations.
7. **JDBC** executes the pure SQL query against MySQL.
8. **MySQL** returns the result set.
9. **JDBC -> DAO -> Service** map the result back to Java Objects.
10. **REST Resource** converts Java Objects to JSON.
11. **Fetch()** receives the JSON response.
12. **DOM Update** triggers a UI change, hiding the loading spinner and showing the Toast notification.

