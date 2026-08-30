# FixFlow User Guide

Welcome to the FixFlow Service Management Platform. This guide explains how to operate the frontend web application based on your assigned role.

## 1. Landing Page
**Location:** `/FixFlow/index.html`
- Introduces the FixFlow platform.
- Provides high-level features and the workflow timeline visualization.
- **Action:** Click "Login" to access an existing account or "Get Started" to register.

## 2. Authentication (Login & Register)
**Location:** `/FixFlow/login.html` & `/FixFlow/register.html`
- **Registration:** Users must provide a Name, Email, Phone, and a secure password. A visual password strength indicator will guide you to create a robust credential.
- **Login:** Enter your email and password. Upon submission, the API is called.
- **Technical Flow:** The server validates the credentials using BCrypt and issues a JSON Web Token (JWT). The frontend stores this token in `localStorage` and redirects you to the appropriate dashboard based on your server-provided role (`USER`, `TECHNICIAN`, `ADMIN`).

## 3. Dark/Light Theme & User Profile
- **Global:** The moon/sun icon in the top right toggles the active theme. This is saved to `localStorage` and applies automatically to all pages.
- **Profile:** Click your avatar in the top right of any dashboard to view your Role and Name, and to trigger a safe `Logout`.

## 4. User Dashboard
**Role required:** `USER`
- **Overview:** Displays summary statistics (Total, Pending, Resolved) for your own requests.
- **My Requests:** A datatable showing all requests you've opened. You can filter by Status or Priority, search by Title/Location, and page through results. 
- **Creating a Request:** Go to "New Request". Fill out the Title, Category (fetched dynamically from the backend), Location, Priority, and Description. Once submitted, it enters the `PENDING` state.
- **Viewing Request:** Click the eye icon to open a modal detailing the request, including a dynamic workflow timeline indicating its current status.

## 5. Technician Dashboard
**Role required:** `TECHNICIAN`
- **Overview:** Displays summary statistics of requests assigned strictly to you.
- **Workflow:** You cannot create requests. You only view requests assigned to you (`ASSIGNED` status).
- **Updating Status:** Open a request in your dashboard. You have the ability to advance it:
  - If `ASSIGNED` -> Click "Start Work" to move it to `IN_PROGRESS`.
  - If `IN_PROGRESS` -> Click "Mark Resolved" to move it to `RESOLVED`.
- The REST API enforces that Technicians cannot close requests. 

## 6. Admin Dashboard
**Role required:** `ADMIN`
- **Overview:** An aggregate view of the entire system (Total, Pending, In Progress, Resolved).
- **Request Management:** View all requests in the system. Admins can Assign Technicians to `PENDING` requests, advancing them to `ASSIGNED`.
- **Closing Requests:** Once a Technician marks a request `RESOLVED`, an Admin can review and mark it `CLOSED`.
- **User & Category Management:** (Advanced Tabs) Admins can view registered users and manage active Categories (adding/editing/deleting).

## 7. Pagination & Search
- Data tables feature built-in pagination (Previous/Next buttons) which alter the `?page=X` query parameters sent to the REST API.
- The Search bar filters by checking both Titles and Locations natively on the backend via optimized SQL `LIKE` queries.
