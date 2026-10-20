# LearnLoop Security Architecture & Compliance

## 1. Authentication & Credential Storage

### Password Security
- Passwords are encrypted before database insertion using **BCrypt** (`BCryptPasswordEncoder`) with 12 strength rounds.
- Plaintext passwords and hashes are never logged, serialized into DTOs, or returned in API responses.

### Token Lifecycle & Session Handling
LearnLoop provides a hybrid, defense-in-depth token strategy:
1. **Stateless JWT**:
   - Algorithm: `HMAC-SHA256` signed with a 256-bit secret loaded strictly via `JWT_SECRET`.
   - Payload: Contains standard claims (`sub` as user UUID, `role`, `issuedAt`, `expiration`).
   - Default expiration: 24 hours (`86400000 ms`).
2. **HttpOnly Cookie for Browsers**:
   - Set on login as `learnloop_token`.
   - Configured with `HttpOnly`, `Path=/`, and `SameSite=Lax`.
   - In production environments, `Secure` is enabled automatically over HTTPS.
   - Prevents client-side JavaScript access, effectively neutralizing XSS-based token theft.
3. **Session Termination**:
   - `POST /api/v1/auth/logout` sets `Max-Age=0` and overwrites the cookie with an empty value.

---

## 2. Authorization & Role-Based Access Control (RBAC)

### Role Hierarchy & Definitions
1. **`ROLE_STUDENT`**:
   - Can browse published courses, enroll, mark lessons complete, view enrolled lesson content.
   - Can ask academic doubts and post discussion replies.
   - Can submit assignments and view only their own feedback and grades.
   - Cannot create courses, alter course publication status, or grade submissions.
2. **`ROLE_TEACHER`**:
   - Can author, edit, and publish courses and lessons they own.
   - Can view student rosters enrolled in their courses.
   - Can answer questions and moderate discussions in their course domains.
   - Can create assignments, review submissions, and award grades and feedback.
   - Cannot modify another teacher's courses or access administrative user management.
3. **`ROLE_ADMIN`**:
   - Can view aggregate platform analytics, user rosters, and outbox event backlogs.
   - Can activate, deactivate, or suspend any user account.
   - Can manage user roles under administrative control.

### Prevention of Privilege Escalation
- **No Self-Registration as Admin**: The registration DTO strictly validates that the requested role is either `STUDENT` or `TEACHER`. Any attempt to send `ADMIN` in the registration payload is rejected server-side with an `IllegalArgumentException` (HTTP 400).
- **Admin Bootstrap Mechanism**: Initial system administrators can only be created via the automated `AdminBootstrapService` utilizing secure environment variables (`ADMIN_BOOTSTRAP_EMAIL`, `ADMIN_BOOTSTRAP_PASSWORD`) on application launch.

---

## 3. Resource Ownership & Content Protection

- **Course Content Redaction**:
  - The public course inspection endpoint (`GET /api/v1/courses/{id}`) exposes course metadata and lesson outlines (titles, positions) to unauthenticated or unenrolled users.
  - However, sensitive lesson payloads (`contentUrlOrText`) are explicitly redacted to `[ENROLLMENT_REQUIRED]` unless the caller holds an active enrollment or is the owning teacher/admin.
- **Data Isolation**:
  - Submissions, grades, and notifications query using `WHERE student_id = :authenticatedUserId` derived directly from the `SecurityContextHolder`. Client-supplied IDs are never trusted for identity resolution.

---

## 4. Injection & Client-Side Attacks

- **SQL Injection**: All database operations use Spring Data JPA repository methods and parameterized JPQL queries with Hibernate, preventing SQL injection vulnerabilities.
- **Cross-Site Scripting (XSS)**:
  - Frontend utilizes a dedicated sanitization helper (`escapeHtml()`) and DOM `textContent` rendering.
  - No raw user inputs are inserted via `innerHTML` without entity encoding.
- **Cross-Origin Resource Sharing (CORS)**:
  - Controlled CORS configuration allows specified origins, headers (`Authorization`, `Content-Type`, `X-Correlation-ID`), and credentials.
