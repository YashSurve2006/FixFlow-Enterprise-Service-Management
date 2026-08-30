# FixFlow — Enterprise Service Request Management System
## Project Presentation & Viva Documentation

---

## 1. Project Title

**FixFlow** — A full-stack Enterprise Service Request Management System built using Jakarta RESTful Web Services, Java 21, MySQL 8, and Vanilla JavaScript with the Fetch API.

---

## 2. Project Overview

FixFlow is a campus/enterprise maintenance management platform that enables registered users to submit service requests (broken equipment, leaking pipes, electrical faults), assigns them to technicians, and tracks them through a rigorous state machine workflow until they are closed by administrators.

The system is **purposefully built without Spring Boot or any MVC framework**, demonstrating deep knowledge of:
- Raw JAX-RS (Jakarta REST) API design
- Manual JDBC database access with transactions
- JWT-based stateless authentication
- BCrypt password security
- Role-Based Access Control (RBAC)
- Vanilla JavaScript `fetch()` API integration

---

## 3. Problem Statement

Campus maintenance systems typically suffer from:

| Problem | Impact |
|---|---|
| No formal request tracking | Requests are lost or forgotten |
| No accountability chain | No clear ownership of maintenance tasks |
| No real-time status visibility | Users unaware if their issue is being handled |
| No role segregation | Anyone can approve/close requests |
| Manual paper-based workflow | Slow, error-prone, difficult to audit |

**FixFlow** solves all of these with a structured, digital, role-aware request lifecycle backed by a production-grade REST API.

---

## 4. Objectives

1. Design a RESTful Web Service API following HTTP standards (GET, POST, PUT, PATCH, DELETE)
2. Implement JWT-based authentication without any external auth framework
3. Enforce Role-Based Authorization using custom JAX-RS annotations
4. Build a real database transaction for technician assignment atomicity
5. Implement search, filtering, sorting, and pagination at the SQL level
6. Create a premium Vanilla JS frontend consuming the Fetch API
7. Support dark/light mode with persistent theme storage
8. Produce a fully tested, viva-ready application

---

## 5. Key Features

| Feature | Description |
|---|---|
| JWT Authentication | Stateless login with 24-hour token lifetime |
| BCrypt Password Hashing | Secure one-way hashing using jbcrypt 0.4 |
| RBAC (3 Roles) | USER, TECHNICIAN, ADMIN with strict permissions |
| Service Request CRUD | Full Create/Read/Update/Delete with validation |
| 5-Stage State Machine | PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED |
| Technician Assignment | ACID transaction covering both assignment + status update |
| Search & Filtering | SQL-level filtering by status, priority, category, location, date |
| Pagination & Sorting | `?page=X&limit=Y&sortBy=Z&sortOrder=asc` query parameters |
| Statistics API | Aggregate breakdown by status, priority, and category |
| Global Exception Handling | Custom ExceptionMapper for consistent JSON error envelopes |
| CORS Support | CorsFilter allowing cross-origin requests for development |
| Premium Frontend | Split-screen auth, role dashboards, animated toasts |
| Dark/Light Theme | CSS variable system with `localStorage` persistence |
| Responsive Design | Works on mobile, tablet, and desktop |

---

## 6. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 21 (LTS) |
| **Build Tool** | Apache Maven | 3.9.6 |
| **Web Standard** | Jakarta EE | Jakarta REST 3.1.0 |
| **REST Framework** | Eclipse Jersey | 3.1.8 |
| **App Server** | Apache Tomcat | 10.1.18 |
| **Database** | MySQL | 8.x |
| **DB Access** | JDBC (pure) | MySQL Connector/J 9.0.0 |
| **Auth Tokens** | JJWT (io.jsonwebtoken) | 0.12.5 |
| **Password Hash** | jbcrypt (org.mindrot) | 0.4 |
| **JSON Binding** | Jersey JSON-B | 3.1.8 |
| **Frontend** | HTML5 / CSS3 / Vanilla JS | — |
| **HTTP Client** | Browser Fetch API | Native |

---

## 7. Why RESTful Web Services?

REST (Representational State Transfer) was chosen because:

1. **Statelessness** — Every request is self-contained. No server-side sessions means the application can scale horizontally.
2. **HTTP Semantics** — Using GET/POST/PUT/PATCH/DELETE aligns with how the web was designed, making the API self-documenting and predictable.
3. **Separation of Concerns** — The backend exposes pure JSON. The frontend is completely decoupled; it could be replaced with a mobile app without changing the backend.
4. **Standard Error Codes** — HTTP status codes (200, 201, 400, 401, 403, 404, 500) communicate intent without inventing a custom protocol.
5. **Interoperability** — Any client that can make HTTP requests can consume this API, regardless of programming language or platform.

---

## 8. System Architecture

```
╔══════════════════════════════════════════════════════════════════╗
║                        CLIENT BROWSER                            ║
║   ┌────────────┐  ┌──────────────┐  ┌───────────────────────┐  ║
║   │ index.html │  │  login.html  │  │ user/dashboard.html    │  ║
║   │register.html│  │              │  │ technician/dashboard   │  ║
║   └────────────┘  └──────────────┘  │ admin/dashboard.html   │  ║
║                                     └───────────────────────┘  ║
║          ┌──────────────────────────────────────────────┐      ║
║          │         JavaScript Modules                    │      ║
║          │  utils.js  api.js  auth.js  user.js           │      ║
║          │  admin.js  technician.js                       │      ║
║          └──────────────┬───────────────────────────────┘      ║
║                         │  Native fetch() with JWT Header       ║
╚═════════════════════════╪════════════════════════════════════════╝
                          │  HTTP/JSON over TCP (port 8080)
╔═════════════════════════╪════════════════════════════════════════╗
║         APACHE TOMCAT 10.1.18 (Application Server)             ║
║                         ↓                                       ║
║         ┌───────────────────────────────────────┐              ║
║         │   Jersey Servlet Dispatcher            │              ║
║         │   @ApplicationPath("/api")             │              ║
║         └────────────────┬──────────────────────┘              ║
║                          ↓                                      ║
║         ┌────────────────────────────────────────┐             ║
║         │   AuthenticationFilter (Priority 1000) │             ║
║         │   @Secured + JWT Validation            │             ║
║         │   → Sets UserSecurityContext           │             ║
║         └────────────────┬───────────────────────┘             ║
║                          ↓                                      ║
║         ┌────────────────────────────────────────┐             ║
║         │           JAX-RS Resources             │             ║
║         │  HealthResource  AuthResource          │             ║
║         │  ServiceRequestResource                │             ║
║         │  ServiceCategoryResource               │             ║
║         │  UserResource  AssignmentResource      │             ║
║         └────────────────┬───────────────────────┘             ║
║                          ↓                                      ║
║         ┌────────────────────────────────────────┐             ║
║         │           Service Layer                │             ║
║         │  AuthService  ServiceRequestService   │             ║
║         │  RequestAssignmentService             │             ║
║         │  ServiceCategoryService  UserService  │             ║
║         └────────────────┬───────────────────────┘             ║
║                          ↓                                      ║
║         ┌────────────────────────────────────────┐             ║
║         │           DAO Layer                    │             ║
║         │  UserDAO  ServiceRequestDAO            │             ║
║         │  ServiceCategoryDAO                    │             ║
║         │  RequestAssignmentDAO                  │             ║
║         └────────────────┬───────────────────────┘             ║
║                          ↓                                      ║
║         ┌────────────────────────────────────────┐             ║
║         │   DatabaseUtil (JDBC Connection)       │             ║
║         │   DriverManager.getConnection()        │             ║
║         │   Credentials from ENV variables       │             ║
║         └────────────────┬───────────────────────┘             ║
╚══════════════════════════╪═════════════════════════════════════╝
                           │  JDBC / MySQL protocol
╔══════════════════════════╪═════════════════════════════════════╗
║        MySQL 8 DATABASE (fixflow_db)                           ║
║         ┌────────────────────────────────────────┐             ║
║         │  users  service_categories             │             ║
║         │  service_requests  request_assignments │             ║
║         └────────────────────────────────────────┘             ║
╚═════════════════════════════════════════════════════════════════╝
```

---

## 9. Complete Request/Response Data Flow

### Action: USER Clicks "Create Request"

```
  [BROWSER]
     │
     ├─ DOM event listener on <form id="form-create-request">
     │  ├─ Reads: title, categoryId, location, priority, description
     │  └─ Calls api.js → apiPost('/requests', payload)
     │
     ├─ fetch('/FixFlow/api/requests', {
     │     method: 'POST',
     │     headers: {
     │       'Content-Type': 'application/json',
     │       'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9...'
     │     },
     │     body: JSON.stringify({ title, categoryId, ... })
     │  })
     │
  [TOMCAT / JERSEY DISPATCHER]
     │
     ├─ Routes to ServiceRequestResource.createRequest()
     │
  [AuthenticationFilter.filter()]
     │
     ├─ Reads "Authorization" header
     ├─ Extracts Bearer token
     ├─ JJWT: Jwts.parser().verifyWith(SECRET_KEY).parseSignedClaims(token)
     ├─ Extracts userId and role from Claims
     ├─ Checks @Secured({Role.USER, Role.ADMIN}) on method
     │   └─ If TECHNICIAN: abortWith(403 Forbidden)
     ├─ Sets UserSecurityContext (userId=1, role=USER)
     │
  [ServiceRequestResource.createRequest()]
     │
     ├─ Reads authUserId from SecurityContext.getUserPrincipal().getName()
     ├─ Calls: service.createRequest(dto, authUserId)
     │
  [ServiceRequestService.createRequest()]
     │
     ├─ validateCreateRequest(dto)
     │   ├─ Checks: title not null/empty
     │   ├─ Checks: description not null/empty
     │   ├─ Checks: location not null/empty
     │   ├─ Checks: priority not null
     │   └─ Checks: categoryId valid (calls categoryDAO.findById())
     │
     ├─ Creates ServiceRequest entity
     │   └─ Sets status = RequestStatus.PENDING  ← hardcoded default
     │
     ├─ Calls: requestDAO.create(req)
     │
  [ServiceRequestDAO.create()]
     │
     ├─ SQL: INSERT INTO service_requests
     │       (user_id, category_id, title, description, priority, status, location)
     │       VALUES (?, ?, ?, ?, ?, ?, ?)
     ├─ PreparedStatement → setInt, setString, etc.
     ├─ executeUpdate()
     ├─ getGeneratedKeys() → new ID
     └─ Returns: findById(newId) for full entity with timestamps
     │
  [MySQL 8 (fixflow_db)]
     │
     └─ INSERT row → Returns auto-increment id
     │
  [Response chain (reverse)]
     │
     ├─ DAO returns ServiceRequest entity
     ├─ Service maps to ServiceRequestResponse DTO
     ├─ Resource wraps in DataResponse<T>
     ├─ Jersey serializes to JSON (JSON-B)
     └─ HTTP 201 Created with body:
        {
          "data": {
            "id": 5,
            "title": "Leaky Pipe",
            "status": "PENDING",
            "priority": "HIGH",
            ...
          }
        }
     │
  [BROWSER]
     │
     ├─ fetch() Promise resolves
     ├─ Toast.show("Request created successfully", "success")
     └─ navigation: switchView('view-my-requests')
```

---

### Action: USER Logs In

```
  [login.html]
     │
     └─ document.getElementById('loginForm').addEventListener('submit', ...)
        ├─ btn.disabled = true
        ├─ btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing in...'
        └─ apiPost('/auth/login', { email, password })
     │
  [AuthResource.login()]
     │
     └─ service.login(loginRequest)
     │
  [AuthService.login()]
     │
     ├─ userDAO.findByEmail(email)
     │   └─ SELECT * FROM users WHERE email = ?
     ├─ BCrypt.checkpw(rawPassword, hashedPassword)   ← timing-safe comparison
     │   └─ If mismatch → throw UnauthorizedException("Invalid email or password")
     │
     ├─ Jwts.builder()
     │   .subject(userId)
     │   .claim("role", "ADMIN")
     │   .issuedAt(now)
     │   .expiration(now + 86400000ms)  ← 24 hours
     │   .signWith(SECRET_KEY)          ← HMAC-SHA256
     │   .compact()
     │
     └─ Returns: AuthResponse { token: "eyJ...", user: {...} }
     │
  [BROWSER]
     │
     ├─ Auth.setAuth(data.token, data.user)
     │   ├─ localStorage.setItem('fixflow_token', token)
     │   └─ localStorage.setItem('fixflow_user', JSON.stringify(user))
     │
     ├─ Toast.show('Login successful', 'success')
     └─ Redirect based on role:
        ├─ ADMIN    → /FixFlow/admin/dashboard.html
        ├─ TECHNICIAN → /FixFlow/technician/dashboard.html
        └─ USER     → /FixFlow/user/dashboard.html
```

---

### Action: ADMIN Assigns Technician

```
  [admin/dashboard.html]
     │
     └─ apiPost(`/requests/${reqId}/assignment`, { technicianId, notes })
        └─ Authorization: 'Bearer <admin_token>'
     │
  [ServiceRequestResource.assignTechnicianToRequest()]
     │
     ├─ @Secured({Role.ADMIN})  ← Filter blocks non-admins with 403
     └─ assignmentService.assignTechnician(requestId, dto, adminUserId)
     │
  [RequestAssignmentService.assignTechnician()]
     │
     ├─ Validate: technicianId not null
     ├─ userDAO.findById(technicianId) → verify role == TECHNICIAN
     ├─ requestDAO.findById(requestId) → verify status == PENDING or ASSIGNED
     ├─ assignmentDAO.findActiveByRequestId(requestId)
     │   └─ If already assigned → mark old assignment completedAt = NOW()
     │
     ├─ conn = DatabaseUtil.getConnection()
     ├─ conn.setAutoCommit(false)           ← BEGIN TRANSACTION
     │
     ├─ try {
     │     assignmentDAO.create(assignment, conn)     ← INSERT request_assignments
     │     if (request.status == PENDING):
     │         requestDAO.updateStatus(id, ASSIGNED, conn)  ← UPDATE service_requests
     │     conn.commit()                              ← COMMIT
     │   } catch (SQLException ex) {
     │     conn.rollback()                            ← ROLLBACK on failure
     │   }
     │
     └─ HTTP 201 Created:
        {
          "data": {
            "id": 3,
            "requestId": 5,
            "technician": { "id": 2, "name": "Mike Tech", "role": "TECHNICIAN" },
            "assignedBy": { "id": 3, "name": "Admin", "role": "ADMIN" },
            "requestStatus": "ASSIGNED",
            "notes": "Please fix immediately",
            "assignedAt": "2026-08-30 11:59:07.0"
          }
        }
     │
  [BROWSER]
     │
     └─ Toast.show("Technician assigned successfully", "success")
        └─ Table row badge updates from "PENDING" to "ASSIGNED"
```

---

### Action: TECHNICIAN Transitions Status

```
  [technician/dashboard.html]
     │
     └─ apiPatch(`/requests/${reqId}/status`, { status: 'IN_PROGRESS' })
        └─ Authorization: 'Bearer <tech_token>'
     │
  [ServiceRequestResource.updateStatus()]
     │
     └─ service.updateStatus(id, newStatus, authUserId, TECHNICIAN)
     │
  [ServiceRequestService.updateStatus()]
     │
     ├─ requestDAO.findById(id) → get current status
     ├─ if role == USER → throw ForbiddenException
     │
     ├─ if role == TECHNICIAN:
     │   ├─ isAssignedTechnician(requestId, authUserId)
     │   │   └─ assignmentDAO.findActiveByRequestId(requestId)
     │   │      └─ active.technicianId == authUserId? else → 403
     │   │
     │   ├─ ASSIGNED → IN_PROGRESS  ✓ (allowed)
     │   ├─ IN_PROGRESS → RESOLVED  ✓ (allowed)
     │   ├─ ASSIGNED → CLOSED       ✗ ValidationException: "Cannot transition..."
     │   └─ ASSIGNED → PENDING      ✗ ValidationException
     │
     ├─ requestDAO.updateStatus(id, newStatus)
     │   └─ UPDATE service_requests SET status = ? WHERE id = ?
     │
     └─ HTTP 200 OK with updated ServiceRequestResponse
```

---

### Action: ADMIN Closes Request

```
  [admin/dashboard.html]
     │
     └─ apiPatch(`/requests/${reqId}/status`, { status: 'CLOSED' })
        └─ Authorization: 'Bearer <admin_token>'
     │
  [ServiceRequestService.updateStatus()]
     │
     ├─ role == ADMIN:
     │   ├─ PENDING → ASSIGNED     ✓ (but admin usually uses /assignment endpoint)
     │   ├─ PENDING → CANCELLED    ✓
     │   ├─ RESOLVED → CLOSED      ✓
     │   ├─ PENDING → CLOSED       ✗ ValidationException
     │   └─ PENDING → IN_PROGRESS  ✗ ValidationException
     │
     ├─ requestDAO.updateStatus(id, CLOSED)
     └─ HTTP 200 OK → Toast.show("Request closed", "success")
```

---

## 10. Frontend Architecture

```
src/main/webapp/
├── index.html               ← Premium landing page
├── login.html               ← Split-screen login with API integration
├── register.html            ← Split-screen registration with password strength
├── css/
│   ├── global.css           ← CSS custom properties, theming, base styles
│   ├── components.css       ← Buttons, cards, modals, toasts, tables, badges
│   ├── dashboard.css        ← Sidebar, topbar, stat cards, layout grid
│   ├── auth.css             ← Split-screen auth page styles
│   └── responsive.css       ← Breakpoints: 1024px, 768px, 430px
├── js/
│   ├── utils.js             ← ThemeManager, Toast, Modal, Format, ViewSwitcher
│   ├── api.js               ← fetch() wrapper: apiGet, apiPost, apiPatch, apiDelete
│   ├── auth.js              ← Auth.getToken(), requireAuth(), logout()
│   ├── user.js              ← User dashboard logic
│   ├── technician.js        ← Technician dashboard logic
│   └── admin.js             ← Admin dashboard logic
├── user/
│   └── dashboard.html       ← Overview, My Requests, New Request views
├── technician/
│   └── dashboard.html       ← Assigned requests, status transitions
└── admin/
    └── dashboard.html       ← All requests, users, categories, statistics
```

**Key Frontend Modules:**

- **`utils.js` — ThemeManager**: Reads `localStorage('fixflow-theme')`, applies `document.documentElement.setAttribute('data-theme', theme)` which triggers CSS variable overrides. Runs synchronously before DOMContentLoaded to prevent flash.
- **`utils.js` — Toast**: Dynamically creates `<div class="toast toast-success">` elements, appends to `#toast-container`, auto-removes after 4 seconds with slide-out animation.
- **`utils.js` — Modal**: Toggles `.active` class on `.modal-backdrop` elements for show/hide.
- **`api.js` — apiRequest()**: Wraps native `fetch()`, automatically attaches JWT from `localStorage`, sets `Content-Type: application/json`, handles global 401 redirects.
- **`auth.js` — requireAuth()**: Guards every dashboard page. If no token in `localStorage`, immediately redirects to `/FixFlow/login.html`. If wrong role, redirects to the correct dashboard.

---

## 11. Backend Architecture

### Package Structure

```
com.fixflow/
├── config/
│   ├── CorsFilter.java          ← ContainerResponseFilter for CORS headers
│   └── RestConfig.java          ← @ApplicationPath("/api") configuration
├── dao/
│   ├── UserDAO.java             ← CRUD for users table
│   ├── ServiceRequestDAO.java   ← CRUD + filtered queries + statistics
│   ├── ServiceCategoryDAO.java  ← CRUD for service_categories
│   └── RequestAssignmentDAO.java← CRUD + active-assignment lookups
├── dto/                         ← 20+ DTO classes for request/response separation
├── exception/
│   ├── GlobalExceptionHandler.java ← ExceptionMapper<Throwable>
│   ├── ResourceNotFoundException   ← Mapped to 404
│   ├── ValidationException         ← Mapped to 400
│   ├── UnauthorizedException       ← Mapped to 401
│   ├── ForbiddenException          ← Mapped to 403
│   └── ConflictException           ← Mapped to 409
├── model/
│   ├── User.java                ← Entity with Role enum
│   ├── ServiceRequest.java      ← Entity with Priority, RequestStatus enums
│   ├── ServiceCategory.java     ← Entity
│   ├── RequestAssignment.java   ← Entity with assignment lifecycle timestamps
│   ├── Role.java                ← Enum: USER, TECHNICIAN, ADMIN
│   ├── Priority.java            ← Enum: LOW, MEDIUM, HIGH, URGENT
│   └── RequestStatus.java       ← Enum: PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
├── resource/                    ← JAX-RS endpoint classes
├── security/
│   ├── AuthenticationFilter.java ← @Priority(AUTHENTICATION) ContainerRequestFilter
│   ├── Secured.java              ← Custom annotation (@interface)
│   └── UserSecurityContext.java  ← Custom SecurityContext implementation
├── service/                     ← Business logic layer
└── util/
    └── DatabaseUtil.java        ← JDBC connection factory using env variables
```

---

## 12. Database Architecture

The database uses **4 tables** with proper foreign keys, indexes, and normalized relationships.

### Environment-Based Credentials (Security Practice)

```java
// DatabaseUtil.java - never hardcoded
private static final String URL = System.getenv("DB_URL");
private static final String USER = System.getenv("DB_USER");
private static final String PASSWORD = System.getenv("DB_PASSWORD");
```

Configured in Tomcat's `setenv.bat`:
```bat
set DB_URL=jdbc:mysql://localhost:3306/fixflow_db
set DB_USER=root
set DB_PASSWORD=<not hardcoded in any source file>
```

---

## 13. Database Tables and Relationships

```
  users (PK: id)
  ┌────────────────────────────────────────────────┐
  │ id  | name      | email         | password      │
  │     |           |               | role          │
  └──────────┬────────────────────────┬─────────────┘
             │  user_id (FK)          │  technician_id (FK)
             ▼                        │  assigned_by   (FK)
  service_requests (PK: id)           │
  ┌──────────────────────────────┐    │
  │ id  | user_id | category_id  │    │
  │     | title   | description  │    │
  │     | priority| status       │    │
  │     | location| created_at   │    │
  └──────────┬───────────────────┘    │
             │  ↓                     │
             │  Indexes:              │
             │    idx_status          │
             │    idx_user_id         │
             │    idx_priority        │
             │    idx_created_at      │
             │                        │
             │ request_id (FK) ◄──────┤
             ▼                        ▼
  request_assignments (PK: id)
  ┌──────────────────────────────────────────────────────┐
  │ id  | request_id | technician_id | assigned_by       │
  │     | assigned_at| accepted_at   | completed_at      │
  │     | notes                                          │
  └──────────────────────────────────────────────────────┘
             Indexes: idx_request_id, idx_technician_id

  service_categories (PK: id)
  ┌─────────────────────────────────────┐
  │ id  | name | description            │
  └─────────────────────────────────────┘
    ↑ category_id FK from service_requests
```

### Foreign Key Cascade Rules
- `service_requests.user_id` → `users.id` **ON DELETE CASCADE** (delete user → delete their requests)
- `service_requests.category_id` → `service_categories.id` **ON DELETE RESTRICT** (cannot delete category with active requests)
- `request_assignments.request_id` → `service_requests.id` **ON DELETE CASCADE**
- `request_assignments.technician_id` → `users.id` **ON DELETE RESTRICT**
- `request_assignments.assigned_by` → `users.id` **ON DELETE RESTRICT**

---

## 14. REST API Endpoint Table

| Method | Endpoint | Auth Required | Role | Description |
|--------|----------|---------------|------|-------------|
| GET | `/api/health` | No | — | Health check |
| POST | `/api/auth/register` | No | — | Register new USER |
| POST | `/api/auth/login` | No | — | Login, get JWT |
| GET | `/api/requests` | Yes | ALL | List requests (filtered) |
| GET | `/api/requests/statistics` | Yes | ADMIN | Aggregate statistics |
| GET | `/api/requests/{id}` | Yes | ALL | Get single request |
| POST | `/api/requests` | Yes | USER, ADMIN | Create request |
| PUT | `/api/requests/{id}` | Yes | USER, ADMIN | Full update |
| PATCH | `/api/requests/{id}` | Yes | USER, ADMIN | Partial update |
| PATCH | `/api/requests/{id}/status` | Yes | TECH, ADMIN | Update status (state machine) |
| DELETE | `/api/requests/{id}` | Yes | USER, ADMIN | Delete request |
| GET | `/api/requests/{id}/assignment` | Yes | ALL | Get assignments for request |
| POST | `/api/requests/{id}/assignment` | Yes | ADMIN | Assign technician |
| GET | `/api/categories` | Yes | ALL | List categories |
| POST | `/api/categories` | Yes | ADMIN | Create category |
| PUT | `/api/categories/{id}` | Yes | ADMIN | Update category |
| DELETE | `/api/categories/{id}` | Yes | ADMIN | Delete category |
| GET | `/api/users` | Yes | ADMIN | List all users |
| GET | `/api/users/{id}` | Yes | ADMIN | Get single user |
| PUT | `/api/users/{id}` | Yes | ADMIN | Update user |
| PATCH | `/api/users/{id}` | Yes | ADMIN | Partial update user |
| DELETE | `/api/users/{id}` | Yes | ADMIN | Delete user |

---

## 15. GET — Read Operations

`GET` is an **idempotent** operation that retrieves data without side effects.

**Example: List filtered requests**
```
GET /api/requests?status=PENDING&priority=HIGH&page=1&limit=10&sortBy=createdAt&sortOrder=desc
Authorization: Bearer eyJhbGci...
```

**Response:**
```json
{
  "data": [ { "id": 1, "title": "...", "status": "PENDING", ... } ],
  "pagination": {
    "page": 1, "limit": 10, "totalItems": 3,
    "totalPages": 1, "hasNext": false, "hasPrevious": false
  }
}
```

Role-based data isolation happens inside the service layer:
- **USER**: `filter.setUserId(authUserId)` → only sees their own requests
- **TECHNICIAN**: `filter.setTechnicianId(authUserId)` + JOIN on `request_assignments` → only assigned requests
- **ADMIN**: no filter applied → sees all requests

---

## 16. POST — Create Operations

`POST` creates a new resource. It is **not idempotent** (calling twice creates two records).

**Example: Create service request**
```
POST /api/requests
Content-Type: application/json
Authorization: Bearer eyJhbGci...

{
  "title": "Broken AC",
  "description": "AC unit in Lab B not working",
  "categoryId": 2,
  "priority": "HIGH",
  "location": "Lab B, Floor 3"
}
```

Server responds with `HTTP 201 Created` and the full resource including its auto-generated `id` and `created_at` timestamp.

---

## 17. PUT — Full Replacement

`PUT` replaces the entire resource. Every field must be provided; missing fields may be set to null.

```
PUT /api/requests/5
Authorization: Bearer eyJhbGci...

{
  "title": "Updated Title",
  "description": "Full new description",
  "priority": "URGENT",
  "status": "PENDING",
  "location": "New Location"
}
```

---

## 18. PATCH — Partial Update

`PATCH` updates only the provided fields. It is more bandwidth-efficient than PUT.

```
PATCH /api/requests/5
Authorization: Bearer eyJhbGci...

{ "priority": "URGENT" }
```

Only `priority` is changed; all other fields remain unchanged. In the service layer, each field is individually null-checked before updating.

The status transition endpoint is also a `PATCH`:
```
PATCH /api/requests/5/status
Authorization: Bearer eyJhbGci...

{ "status": "IN_PROGRESS" }
```

---

## 19. DELETE — Remove Resource

`DELETE` permanently removes a resource.

```
DELETE /api/requests/5
Authorization: Bearer eyJhbGci...
```

- USERs can only delete their own requests
- ADMINs can delete any request
- Returns `HTTP 204 No Content` (empty body, as the resource no longer exists)

---

## 20. Fetch API Integration

The frontend uses the native browser `fetch()` API exclusively. All API calls are routed through a central wrapper in `api.js`:

```javascript
// api.js - Central fetch wrapper
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;        // e.g. /FixFlow/api/requests

    const headers = { 'Accept': 'application/json', ...options.headers };

    // Automatically attach JWT from localStorage
    const token = localStorage.getItem('fixflow_token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Auto set Content-Type for JSON bodies
    if (options.body && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
        if (typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }
    }

    const response = await fetch(url, { ...options, headers });

    // Global 401 handler — redirect to login
    if (response.status === 401) {
        localStorage.removeItem('fixflow_token');
        window.location.href = '/FixFlow/login.html';
        throw new Error('Unauthorized');
    }

    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'API Request Failed');
    return data;
}

// Helper aliases used throughout dashboard scripts
const apiGet    = (endpoint)       => apiRequest(endpoint, { method: 'GET' });
const apiPost   = (endpoint, body) => apiRequest(endpoint, { method: 'POST', body });
const apiPut    = (endpoint, body) => apiRequest(endpoint, { method: 'PUT', body });
const apiPatch  = (endpoint, body) => apiRequest(endpoint, { method: 'PATCH', body });
const apiDelete = (endpoint)       => apiRequest(endpoint, { method: 'DELETE' });
```

---

## 21. Authentication Flow

```
                    POST /api/auth/login
                    { email, password }
                          │
              ┌───────────▼───────────┐
              │     AuthResource      │
              │     (No @Secured)     │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │     AuthService       │
              │  1. findByEmail()     │
              │  2. BCrypt.checkpw()  │
              │  3. Jwts.builder()    │
              └───────────┬───────────┘
                          │
                          ▼
              JWT Token (valid 24 hours):
              Header:  { "alg": "HS256" }
              Payload: {
                "sub": "1",          ← userId
                "role": "ADMIN",
                "iat": 1724996400,
                "exp": 1725082800
              }
              Signature: HMAC-SHA256(header.payload, SECRET_KEY)
                          │
              ┌───────────▼───────────┐
              │ Browser localStorage  │
              │ fixflow_token = "eyJ" │
              │ fixflow_user = {...}  │
              └───────────────────────┘
```

---

## 22. JWT Implementation

Library: **JJWT 0.12.5** (`io.jsonwebtoken`)

**Token Generation (AuthService.java):**
```java
String token = Jwts.builder()
    .subject(String.valueOf(user.getId()))    // "sub" = userId
    .claim("role", user.getRole().name())     // custom claim
    .issuedAt(new Date())                     // "iat"
    .expiration(new Date(System.currentTimeMillis() + 86400000)) // "exp" = 24h
    .signWith(SECRET_KEY)                     // HMAC-SHA256
    .compact();
```

**Token Validation (AuthenticationFilter.java):**
```java
Claims claims = Jwts.parser()
    .verifyWith(SECRET_KEY)
    .build()
    .parseSignedClaims(token)
    .getPayload();

Integer userId = Integer.valueOf(claims.getSubject());
Role role = Role.valueOf(claims.get("role", String.class));
requestContext.setSecurityContext(new UserSecurityContext(userId, role));
```

**Secret Key**: Loaded from `JWT_SECRET` environment variable. The application emits a `WARNING` if the env variable is not set.

---

## 23. BCrypt Password Security

Library: **jbcrypt 0.4** (`org.mindrot.jbcrypt`)

**Registration — Hash before storing:**
```java
// BCrypt.gensalt() generates a random 16-byte salt
// BCrypt.hashpw() applies 2^10 (default cost) rounds of Blowfish
user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
```

**Login — Verify without decryption:**
```java
// BCrypt.checkpw() is timing-safe — it always takes the same time
// regardless of whether the password matches, preventing timing attacks
if (!BCrypt.checkpw(rawPassword, storedHash)) {
    throw new UnauthorizedException("Invalid email or password");
}
```

Database stores: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- `$2a$` = BCrypt algorithm version (compatible with jbcrypt)
- `$10$` = Cost factor (2^10 = 1024 rounds)
- Next 22 chars = base64-encoded salt
- Remainder = hashed password

**Passwords are NEVER stored in plaintext.**

---

## 24. Role-Based Authorization

**Custom `@Secured` Annotation:**
```java
@Secured            // requires any authenticated user
@Secured({Role.ADMIN})              // requires ADMIN only
@Secured({Role.USER, Role.ADMIN})   // requires USER or ADMIN
```

**`AuthenticationFilter` checks at both class and method level:**
```java
// Class-level: @Secured on ServiceRequestResource → authentication required
checkPermissions(resourceClass, role, requestContext);
// Method-level: @Secured({Role.ADMIN}) on createCategory() → ADMIN only
checkPermissions(resourceMethod, role, requestContext);
```

| Action | USER | TECHNICIAN | ADMIN |
|--------|------|-----------|-------|
| View own requests | ✅ | — | ✅ |
| View assigned requests | — | ✅ | ✅ |
| Create request | ✅ | ❌ | ✅ |
| Assign technician | ❌ | ❌ | ✅ |
| Update status (ASSIGNED→IN_PROGRESS) | ❌ | ✅ | ✅ |
| Update status (RESOLVED→CLOSED) | ❌ | ❌ | ✅ |
| View all requests | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| Manage categories | ❌ | ❌ | ✅ |
| View statistics | ❌ | ❌ | ✅ |

---

## 25. USER Workflow

```
REGISTER / LOGIN
     │
     ▼
USER DASHBOARD (user/dashboard.html)
     │
     ├─ View Overview (stat cards: Total, Pending, Resolved)
     │
     ├─ View My Requests
     │   ├─ Filter by Status / Priority
     │   ├─ Search by title/location
     │   ├─ Paginate results
     │   └─ Click eye icon → View Request Details modal
     │       └─ See workflow timeline (PENDING → ASSIGNED → etc.)
     │
     └─ New Request
         ├─ Fill: Title, Category (dropdown), Location, Priority, Description
         └─ Submit → POST /api/requests → Toast "Request created" → Redirects to My Requests
```

---

## 26. TECHNICIAN Workflow

```
LOGIN (TECHNICIAN credentials)
     │
     ▼
TECHNICIAN DASHBOARD (technician/dashboard.html)
     │
     ├─ View Overview (stat cards for assigned requests)
     │
     └─ View Assigned Requests
         ├─ Only requests where technician_id = this user
         ├─ Status = ASSIGNED:
         │   └─ Click "Start Work" → PATCH /api/requests/{id}/status { status: "IN_PROGRESS" }
         │
         ├─ Status = IN_PROGRESS:
         │   └─ Click "Mark Resolved" → PATCH /api/requests/{id}/status { status: "RESOLVED" }
         │
         └─ Status = RESOLVED/CLOSED: Read-only view
```

---

## 27. ADMIN Workflow

```
LOGIN (ADMIN credentials)
     │
     ▼
ADMIN DASHBOARD (admin/dashboard.html)
     │
     ├─ View Statistics
     │   └─ GET /api/requests/statistics → Aggregate by status, priority, category
     │
     ├─ Request Management (All requests)
     │   ├─ Filter/Search/Sort/Paginate
     │   ├─ PENDING request → "Assign Technician" modal
     │   │   └─ POST /api/requests/{id}/assignment → ASSIGNED
     │   │
     │   ├─ RESOLVED request → "Close" button
     │   │   └─ PATCH /api/requests/{id}/status → CLOSED
     │   │
     │   └─ DELETE any request
     │
     ├─ User Management
     │   └─ GET /api/users → View all users, Edit roles
     │
     └─ Category Management
         ├─ GET /api/categories → List
         ├─ POST /api/categories → Create
         ├─ PUT /api/categories/{id} → Update
         └─ DELETE /api/categories/{id} → Delete
```

---

## 28. Technician Assignment

The assignment operation is the most technically significant part of the workflow because it involves **two database operations that must succeed or fail together**.

**Business Rules (enforced in `RequestAssignmentService`):**
1. Only `ADMIN` can assign (enforced by `@Secured({Role.ADMIN})`)
2. The selected user must have role = `TECHNICIAN`
3. The request must be in `PENDING` or `ASSIGNED` status
4. If a technician is already assigned, the old assignment record has `completed_at` set to NOW() (soft-deactivation), then a new assignment is created
5. When a `PENDING` request is assigned → status updates to `ASSIGNED` atomically

**JDBC Transaction Code:**
```java
try (Connection conn = DatabaseUtil.getConnection()) {
    conn.setAutoCommit(false);  // BEGIN TRANSACTION
    try {
        assignmentDAO.create(assignment, conn);  // INSERT into request_assignments
        if (request.getStatus() == PENDING) {
            requestDAO.updateStatus(id, ASSIGNED, conn);  // UPDATE service_requests
        }
        conn.commit();  // COMMIT
    } catch (SQLException ex) {
        conn.rollback();  // ROLLBACK — no partial data
        throw new RuntimeException("Transaction failed", ex);
    }
}
```

---

## 29. Service Request State Machine

```
                    ┌─────────┐
                    │         │
              ┌────►│ PENDING │◄────── (Initial state, set by Service layer)
              │     │         │
              │     └────┬────┘
              │          │ ADMIN assigns technician
              │          │ (via POST /api/requests/{id}/assignment)
              │          ▼
              │     ┌──────────┐
              │     │          │
              │     │ ASSIGNED │◄──── ADMIN can re-assign
              │     │          │
              │     └────┬─────┘
              │          │ TECHNICIAN starts work
              │          │ (PATCH /status → IN_PROGRESS)
              │          ▼
              │     ┌────────────┐
              │     │            │
              │     │ IN_PROGRESS│
              │     │            │
              │     └─────┬──────┘
              │           │ TECHNICIAN marks resolved
              │           │ (PATCH /status → RESOLVED)
              │           ▼
              │     ┌──────────┐
              │     │          │
              │     │ RESOLVED │
              │     │          │
              │     └────┬─────┘
              │          │ ADMIN closes
              │          │ (PATCH /status → CLOSED)
              │          ▼
              │     ┌────────┐
              │     │        │
              │     │ CLOSED │  (Terminal state)
              │     │        │
              │     └────────┘
              │
              │  ADMIN can also cancel from PENDING:
              └──────────────────────►┌───────────┐
                                      │ CANCELLED │ (Terminal)
                                      └───────────┘

INVALID TRANSITIONS (rejected with HTTP 400):
  PENDING     → IN_PROGRESS  (skip ASSIGNED)
  PENDING     → RESOLVED     (skip all)
  PENDING     → CLOSED       (Admin can only cancel or assign)
  CLOSED      → any          (Terminal state)
  TECHNICIAN  → CLOSED       (Technicians cannot close)
  USER        → any status   (Users cannot change status at all)
```

---

## 30. JDBC Transaction Management

FixFlow uses raw JDBC with **manual transaction demarcation** for critical atomic operations.

**Pattern used for Technician Assignment:**

```java
// Step 1: Get connection from DriverManager
Connection conn = DatabaseUtil.getConnection();

// Step 2: Disable auto-commit to start a logical transaction
conn.setAutoCommit(false);

try {
    // Step 3a: Execute first SQL operation (same connection = same transaction)
    assignmentDAO.create(assignment, conn);

    // Step 3b: Execute second SQL operation
    requestDAO.updateStatus(requestId, ASSIGNED, conn);

    // Step 4: Commit both operations atomically
    conn.commit();

} catch (SQLException ex) {
    // Step 5: On any failure, roll back both operations
    conn.rollback();  // Database returns to the state before setAutoCommit(false)
    throw new RuntimeException("Transaction failed", ex);
}
```

**Why this matters:** Without transactions, if the assignment INSERT succeeds but the status UPDATE fails, the database would be in an inconsistent state — a technician would appear assigned, but the request would still show `PENDING`. The `ROLLBACK` guarantees this never happens.

---

## 31. Search

The search feature performs a **case-insensitive full-text search** across three fields simultaneously.

**Service layer validation:**
```java
// ServiceRequestService.java - sortBy whitelist prevents SQL injection
if (!filter.getSortBy().matches("^(createdAt|priority|status|title)$")) {
    throw new ValidationException("Invalid sortBy field.");
}
```

**DAO SQL generation:**
```java
// ServiceRequestDAO.java
if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
    sql.append(" AND (LOWER(r.title) LIKE LOWER(?) " +
               "OR LOWER(r.description) LIKE LOWER(?) " +
               "OR LOWER(r.location) LIKE LOWER(?)) ");
    String searchPattern = "%" + filter.getSearch().trim() + "%";
    params.add(searchPattern);  // ? bound safely via PreparedStatement
    params.add(searchPattern);
    params.add(searchPattern);
}
```

Query: `GET /api/requests?search=pipe` finds requests with "pipe" in title, description, or location.

---

## 32. Filtering

Multiple filters can be combined and are all applied as safe `PreparedStatement` `?` parameters:

```java
// Each filter appends to WHERE clause using ?
if (status != null)      sql.append(" AND r.status = ? ");
if (priority != null)    sql.append(" AND r.priority = ? ");
if (categoryId != null)  sql.append(" AND r.category_id = ? ");
if (location != null)    sql.append(" AND r.location = ? ");
if (fromDate != null)    sql.append(" AND r.created_at >= ? ");
if (toDate != null)      sql.append(" AND r.created_at <= ? ");
```

Example: `GET /api/requests?status=ASSIGNED&priority=HIGH&categoryId=2`

---

## 33. Sorting

Sort columns are validated against a **strict whitelist** in the service layer to prevent SQL injection through the `ORDER BY` clause:

```java
// Only these 4 values are allowed as sortBy
private String mapSortColumn(String sortBy) {
    if ("createdAt".equals(sortBy)) return "created_at";
    if ("priority".equals(sortBy))  return "priority";
    if ("status".equals(sortBy))    return "status";
    if ("title".equals(sortBy))     return "title";
    return null;  // anything else → no ORDER BY applied
}
```

Sort order (`asc`/`desc`) is also validated with a regex: `^(asc|desc|ASC|DESC)$`.

Example: `GET /api/requests?sortBy=createdAt&sortOrder=desc`

---

## 34. Pagination

Pagination uses SQL `LIMIT` and `OFFSET` clauses with a separate `COUNT` query for metadata:

```java
// Page 2, 10 items per page: OFFSET = (2-1)*10 = 10
sql.append(" LIMIT ? OFFSET ?");
params.add(filter.getLimit());                            // = 10
params.add((filter.getPage() - 1) * filter.getLimit());  // = 10

// Separate count query (same WHERE conditions, no LIMIT/OFFSET)
long totalItems = requestDAO.countFiltered(filter);
```

**PaginationMeta response object:**
```json
{
  "pagination": {
    "page": 2,
    "limit": 10,
    "totalItems": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": true
  }
}
```

Limits are validated: `page >= 1`, `1 <= limit <= 100`.

---

## 35. Statistics

Available only to `ADMIN` role via `GET /api/requests/statistics`.

The DAO executes **four separate SQL queries** on a single connection and aggregates results:

```sql
-- Total count
SELECT COUNT(*) FROM service_requests;

-- Count by status
SELECT status, COUNT(*) FROM service_requests GROUP BY status;

-- Urgent count
SELECT COUNT(*) FROM service_requests WHERE priority = 'URGENT';

-- Requests per category
SELECT c.name, COUNT(r.id)
FROM service_categories c
LEFT JOIN service_requests r ON c.id = r.category_id
GROUP BY c.name;
```

**Response:**
```json
{
  "data": {
    "totalRequests": 15,
    "pending": 3,
    "assigned": 4,
    "inProgress": 2,
    "resolved": 5,
    "closed": 1,
    "urgent": 2,
    "requestsByCategory": [
      { "categoryName": "Electrical", "count": 6 },
      { "categoryName": "Plumbing", "count": 9 }
    ]
  }
}
```

---

## 36. Error Handling

`GlobalExceptionHandler` implements `ExceptionMapper<Throwable>` and maps every exception type to a consistent JSON error envelope:

```java
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        // Hierarchical checks for each custom exception type
        if (exception instanceof ResourceNotFoundException) → 404
        if (exception instanceof ValidationException)       → 400
        if (exception instanceof UnauthorizedException)    → 401
        if (exception instanceof ForbiddenException)       → 403
        if (exception instanceof ConflictException)        → 409
        if (exception instanceof WebApplicationException)  → use its status
        else                                               → 500 (generic)
    }
}
```

**All errors return the same JSON structure:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this operation"
}
```

**Stack traces are never exposed to the client** — `exception.printStackTrace()` writes only to the server console.

---

## 37. HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, PATCH operations |
| 201 | Created | Successful POST creating a new resource |
| 204 | No Content | Successful DELETE (no body returned) |
| 400 | Bad Request | Validation failure, invalid enum value, invalid transition |
| 401 | Unauthorized | Missing/invalid/expired JWT token |
| 403 | Forbidden | Authenticated but insufficient role |
| 404 | Not Found | Resource ID does not exist |
| 409 | Conflict | Email already registered, duplicate active assignment |
| 500 | Internal Server Error | Unhandled exceptions (database errors) |

---

## 38. CORS

`CorsFilter` implements `ContainerResponseFilter` and adds CORS headers to every response:

```java
// CorsFilter.java
responseContext.getHeaders().add("Access-Control-Allow-Headers",
    "origin, content-type, accept, authorization");
responseContext.getHeaders().add("Access-Control-Allow-Methods",
    "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
```

Origins `http://localhost:3000` and `http://127.0.0.1:3000` are whitelisted (avoids using wildcard `*` which would block `Authorization` headers in credentialed requests).

---

## 39. Dark/Light Theme

The theme system works entirely through **CSS Custom Properties (variables)**:

**`global.css` — Light theme (default):**
```css
:root {
  --bg-main: #f8fafc;
  --bg-surface: #ffffff;
  --text-primary: #1a202c;
  --primary-color: #4f46e5;
  /* ... */
}
```

**`global.css` — Dark theme override:**
```css
[data-theme="dark"] {
  --bg-main: #0f172a;
  --bg-surface: #1e293b;
  --text-primary: #f1f5f9;
  /* ... */
}
```

**`utils.js` — Applies synchronously before DOMContentLoaded:**
```javascript
// Runs as IIFE to prevent flash of incorrect theme
(function() {
    const savedTheme = localStorage.getItem('fixflow-theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();
```

Toggle stored in `localStorage` under key `fixflow-theme`. Applying `data-theme="dark"` to `<html>` triggers the CSS variable overrides cascade-wide.

---

## 40. Responsive UI/UX

CSS breakpoints defined in `responsive.css`:

| Breakpoint | Target | Changes |
|---|---|---|
| < 1024px | Tablet | Sidebar collapses to horizontal scroll nav |
| < 768px | Tablet portrait | Stat cards stack vertically, tables scroll horizontally |
| < 430px | Mobile | Full-width forms, minimal padding, single-column layout |

The frontend also includes:
- **Loading states**: Buttons show `<i class="fas fa-spinner fa-spin">` during API calls
- **Toast notifications**: Slide in from bottom-right, auto-dismiss after 4 seconds
- **Empty states**: Tables show "No requests found" instead of empty blank areas
- **Avatar initials**: User's first name initial shown in the topbar avatar circle

---

## 41. Security Measures

| Measure | Implementation |
|---|---|
| No plaintext passwords | BCrypt with random salt (`BCrypt.gensalt()`) |
| No SQL injection | 100% `PreparedStatement` with `?` parameters |
| No JWT in HTML/JS | Token stored only in `localStorage`, never in source code |
| No hardcoded DB creds | All credentials from OS environment variables |
| Sortable columns whitelist | `mapSortColumn()` returns null for unknown fields |
| Role verification | Custom `@Secured` annotation + `AuthenticationFilter` |
| Token expiry | JWT expires after 24 hours |
| CORS restriction | Only whitelisted origins (not wildcard `*`) |
| No stack traces to client | `GlobalExceptionHandler` intercepts all exceptions |
| Resource ownership check | USERs verified as owner before read/write access |

---

## 42. Testing & QA Results

### Infrastructure

| Test | Result |
|------|--------|
| `mvn clean package` → BUILD SUCCESS | **PASS** |
| Tomcat 10.1.18 WAR deployment | **PASS** |
| GET /api/health → `{"application":"FixFlow","status":"UP"}` | **PASS** |

### Authentication (executed via PowerShell against live server)

| Test | Expected | Result |
|------|----------|--------|
| Admin Login (valid credentials) | HTTP 200 + JWT | **PASS** |
| Technician Login (valid credentials) | HTTP 200 + JWT | **PASS** |
| User Login (valid credentials) | HTTP 200 + JWT | **PASS** |
| Login with wrong password | HTTP 401 | **PASS** |

### Authorization

| Test | Expected | Result |
|------|----------|--------|
| No token → GET /api/users | HTTP 401 | **PASS** |
| USER token → GET /api/users | HTTP 403 | **PASS** |
| TECHNICIAN token → GET /api/users | HTTP 403 | **PASS** |
| ADMIN token → GET /api/users | HTTP 200 | **PASS** |

### Workflow (Full End-to-End)

| Test | Expected | Result |
|------|----------|--------|
| User creates request | HTTP 201, status=PENDING | **PASS** |
| Tech attempts to assign → blocked | HTTP 403 | **PASS** |
| Admin assigns technician | HTTP 201, status=ASSIGNED | **PASS** |
| Tech transitions ASSIGNED→IN_PROGRESS | HTTP 200 | **PASS** |
| Tech attempts CLOSED (invalid transition) | HTTP 400 | **PASS** |
| Tech transitions IN_PROGRESS→RESOLVED | HTTP 200 | **PASS** |
| Admin transitions RESOLVED→CLOSED | HTTP 200 | **PASS** |

### Search / Filter / Pagination

| Test | Expected | Result |
|------|----------|--------|
| GET /api/requests?page=1&limit=5 | Paginated JSON with meta | **PASS** |
| GET /api/requests?status=CLOSED | Filtered results | **PASS** |
| Sort injection attempt (invalid sortBy) | HTTP 400 ValidationException | **PASS** |

### Database Security

| Test | Expected | Result |
|------|----------|--------|
| All DAOs use PreparedStatement | No string concat for values | **PASS** |
| Passwords stored as BCrypt hash | No plaintext in DB | **PASS** |
| Credentials not in any source file | Env variable only | **PASS** |

---

## 43. Advantages

1. **Zero framework dependency for core logic** — Demonstrates real HTTP and REST knowledge without hiding complexity behind Spring magic
2. **Clean architecture** — Resource → Service → DAO → Model layers with strict separation
3. **Proper ACID transactions** — Manual `setAutoCommit(false)` / `rollback()` ensures data consistency
4. **Production-grade security** — BCrypt, JWT, RBAC, no SQL injection, no secrets in code
5. **Developer-friendly API** — Consistent JSON envelopes, useful error messages, proper HTTP status codes
6. **Scalable backend** — Stateless JWT means no session storage; multiple Tomcat instances can run simultaneously
7. **Premium frontend** — Dark mode, toast notifications, responsive design, no external CSS frameworks
8. **Documented and tested** — ARCHITECTURE.md, USER_GUIDE.md, VIVA_NOTES.md, and a QA test script

---

## 44. Limitations

1. **No connection pooling** — `DatabaseUtil` uses `DriverManager.getConnection()` directly. Under heavy load this creates a new TCP connection per request. Production would require HikariCP or c3p0.
2. **No persistent session invalidation** — JWTs cannot be explicitly revoked (logout only clears localStorage client-side); a stolen unexpired token would still work until expiry.
3. **CORS hardcoded** — The whitelist in `CorsFilter.java` must be modified to allow other client origins.
4. **No file attachments** — Service requests cannot include photos or documents.
5. **Single-node only** — No distributed session or token store, limiting multi-server deployment.
6. **No email notifications** — Technicians and users don't receive emails when their request status changes.

---

## 45. Future Scope

| Feature | Technical Approach |
|---|---|
| Connection Pooling | Integrate HikariCP via pom.xml |
| Token Blacklisting | Redis store for revoked JWTs |
| Email Notifications | JavaMail / SMTP on status transitions |
| File Attachments | Multipart form-data, store to filesystem/S3 |
| Audit Logging | `audit_log` table tracking every status change with timestamp |
| Mobile App | Same REST API consumed by an Android/iOS client |
| Refresh Tokens | Separate long-lived refresh token for UX improvement |
| API Rate Limiting | Jersey ContainerRequestFilter with in-memory counter |

---

## 46. Conclusion

FixFlow demonstrates the complete implementation of a production-ready RESTful web service from database schema to browser UI without relying on any high-level framework like Spring Boot. Every layer was coded deliberately:

- **JDBC** was chosen over JPA/Hibernate to show true SQL literacy and transaction control
- **Jakarta REST** was chosen over Spring MVC to demonstrate servlet lifecycle understanding
- **Vanilla JavaScript** was chosen over React to show mastery of the browser's native Fetch API
- **Custom JWT/BCrypt** was chosen over Spring Security to show real understanding of authentication protocols

The result is an application that is:
- **Functionally complete** (all CRUD operations, full workflow, RBAC)
- **Architecturally sound** (proper layering, DTOs, exception handling)
- **Secure** (no SQL injection, no plaintext passwords, no JWT in source files)
- **Production-ready** (environment-based config, transaction management, pagination)
- **Presentation-ready** (premium UI, dark mode, responsive design, QA tested)

---

## How to Explain FixFlow in 2 Minutes

> "FixFlow is a campus maintenance management system. Users log in and submit service requests — like a broken AC or leaking pipe. An admin then assigns a technician to the request. The technician updates the status from Assigned to In Progress when they start working, then to Resolved when done. The admin reviews and closes it.
>
> The backend is a REST API built on Java with Jakarta REST and Jersey, running on Apache Tomcat. Authentication uses JWT tokens — after login, the token travels in every API request header. Passwords are hashed with BCrypt, so even if the database is stolen, passwords cannot be recovered. Role-based authorization uses a custom annotation `@Secured` that blocks wrong roles at the filter layer before any business logic runs.
>
> The database is MySQL with four tables. The technician assignment is the most interesting part technically — it runs as a JDBC transaction: inserting the assignment record and updating the request status in one atomic operation with rollback if either fails.
>
> The frontend is pure HTML and JavaScript using the browser's native Fetch API. No React, no Angular. A central api.js automatically attaches the JWT header to every request. The whole UI supports dark and light mode using CSS variables."

---

## Most Important Viva Points

### 1. Why not Spring Boot?
> Spring Boot auto-configures everything. Using raw JAX-RS means we explicitly configure the Jersey servlet, handle dependency injection manually, and write every filter, mapper, and lifecycle hook ourselves. This demonstrates understanding of what Spring Boot hides.

### 2. How does JWT authentication work?
> At login, the server creates a base64-encoded JSON payload containing `userId` and `role`, then signs it with HMAC-SHA256 using a server-side secret. The client stores the token and sends it in the `Authorization: Bearer` header. The server re-verifies the HMAC signature on every request — no database lookup needed, making it truly stateless.

### 3. What makes BCrypt secure?
> BCrypt is a one-way hash function using the Blowfish cipher. Each hash includes a random 16-byte salt, so identical passwords produce completely different hashes. The cost factor (10 rounds = 2^10 = 1024 iterations) is configurable — increasing it makes brute-force attacks proportionally slower.

### 4. How does the JDBC transaction work?
> The assignment operation calls `conn.setAutoCommit(false)`, then runs two SQL statements on the same connection object (INSERT into request_assignments, UPDATE service_requests). If both succeed, `conn.commit()` persists both atomically. If either fails, `conn.rollback()` reverts all changes — the database never sees a partial state.

### 5. How is SQL injection prevented?
> Every SQL value is bound through `PreparedStatement.setString()` / `setInt()`. The database driver sends the SQL template and parameters separately, so user input is never interpreted as executable SQL. For the `ORDER BY` clause (which cannot use parameters), a strict whitelist function `mapSortColumn()` only allows exactly 4 known safe column names.

### 6. What is the State Machine and why can't users skip states?
> The state machine is enforced in `ServiceRequestService.updateStatus()` with explicit if-conditions per role per transition. `TECHNICIAN` can only go from ASSIGNED→IN_PROGRESS or IN_PROGRESS→RESOLVED. Invalid transitions throw a `ValidationException` mapped to HTTP 400. This prevents, for example, a technician from directly closing a request without an admin review.

### 7. How does pagination work?
> The database query uses `LIMIT ? OFFSET ?`. Offset is calculated as `(page - 1) * limit`. A second identical query with `COUNT(*)` and no LIMIT gets the total count. This is returned as a `PaginationMeta` object alongside the data array, giving the client enough information to render Previous/Next controls.

### 8. How does the Fetch API integrate?
> The central `apiRequest()` function in `api.js` wraps the browser's native `fetch()`. It automatically reads the JWT from `localStorage`, attaches it as the `Authorization: Bearer` header, stringifies request bodies to JSON, and globally intercepts 401 responses to redirect to the login page. This single file is the exclusive bridge between the DOM and the REST API.
