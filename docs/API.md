# LearnLoop REST API Specification (`/api/v1`)

## 1. Conventions & Standards

All LearnLoop endpoints are exposed under `/api/v1`.

### Authentication
Endpoints support two authentication modes seamlessly:
1. **Cookie-Based (PWA/Browser)**: An `HttpOnly`, `SameSite=Lax` cookie named `learnloop_token` is set on login.
2. **Bearer Token (Mobile/API Clients)**: `Authorization: Bearer <jwt-token>` header.

### Standard Response Envelope (`ApiResponse<T>`)
Every successful JSON response follows a predictable envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-20T21:40:00Z"
}
```

### Standard Error Envelope (`ErrorResponse`)
Errors return RFC 7807-compliant payloads without exposing server stack traces:
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Course with ID 'f3a8b2c1-...' not found",
  "status": 404,
  "correlationId": "4bf438b4-ef44-42de-8df8-...",
  "timestamp": "2026-10-20T21:40:00Z",
  "validationErrors": null
}
```

---

## 2. Endpoint Catalog

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register student or teacher account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate with email/password; returns JWT + sets cookie |
| `POST` | `/api/v1/auth/logout` | Authenticated | Clears auth cookie and invalidates session |
| `GET` | `/api/v1/auth/me` | Authenticated | Returns currently authenticated user profile |

### User Management (`/api/v1/users`, `/api/v1/admin/users`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users/me` | Authenticated | Get detailed profile |
| `PATCH` | `/api/v1/users/me` | Authenticated | Update full name, avatar URL |
| `GET` | `/api/v1/admin/users` | `ADMIN` | Search & paginate users by query or role |
| `PATCH` | `/api/v1/admin/users/{id}/status` | `ADMIN` | Activate or suspend user account |
| `PATCH` | `/api/v1/admin/users/{id}/role` | `ADMIN` | Update role (`STUDENT`, `TEACHER`, `ADMIN`) |

### Courses & Lessons (`/api/v1/courses`, `/api/v1/teacher/courses`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/courses` | Public | Search published courses with pagination and filters |
| `GET` | `/api/v1/courses/{id}` | Public | View course overview and syllabus (unauthorized content is redacted) |
| `POST` | `/api/v1/teacher/courses` | `TEACHER` | Create a new course |
| `GET` | `/api/v1/teacher/courses` | `TEACHER` | List teacher's owned courses |
| `PATCH` | `/api/v1/teacher/courses/{id}` | `TEACHER` | Update course details |
| `POST` | `/api/v1/teacher/courses/{id}/publish` | `TEACHER` | Toggle draft / published state |
| `POST` | `/api/v1/teacher/courses/{id}/lessons` | `TEACHER` | Create and append a lesson |
| `PATCH` | `/api/v1/teacher/lessons/{id}` | `TEACHER` | Edit lesson title, content, or position |
| `DELETE` | `/api/v1/teacher/lessons/{id}` | `TEACHER` | Remove a lesson from course |

### Enrollments & Learning Progress (`/api/v1/courses/{id}/enroll`, `/api/v1/students/me`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/courses/{id}/enroll` | `STUDENT` | Enroll in course; triggers transactional outbox event |
| `GET` | `/api/v1/students/me/enrollments` | `STUDENT` | List enrolled courses with completion percentages |
| `POST` | `/api/v1/lessons/{id}/complete` | `STUDENT` | Mark lesson complete; recalculates course progress |
| `GET` | `/api/v1/teacher/courses/{id}/students` | `TEACHER` | View student roster and progress for course |

### Academic Doubts & Discussions (`/api/v1/questions`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/questions` | Authenticated | Browse doubts with course, lesson, and status filters |
| `POST` | `/api/v1/questions` | `STUDENT` | Post academic question regarding a course |
| `GET` | `/api/v1/questions/{id}` | Authenticated | Get question thread and replies |
| `POST` | `/api/v1/questions/{id}/replies` | Authenticated | Post reply; triggers Kafka notification to author |
| `PATCH` | `/api/v1/questions/{id}/status` | Authenticated | Mark question as `RESOLVED` (Author or Teacher) |

### Assignments & Grading (`/api/v1/courses/{id}/assignments`, `/api/v1/assignments`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/teacher/courses/{id}/assignments` | `TEACHER` | Create coursework assignment with due date and max marks |
| `GET` | `/api/v1/courses/{id}/assignments` | Authenticated | View assignments for course |
| `POST` | `/api/v1/assignments/{id}/submissions` | `STUDENT` | Submit assignment text and external resource link |
| `GET` | `/api/v1/assignments/{id}/my-submission` | `STUDENT` | View own submission, marks, and feedback |
| `GET` | `/api/v1/teacher/assignments/{id}/submissions` | `TEACHER` | Review student submissions for assignment |
| `PATCH` | `/api/v1/teacher/submissions/{id}/grade` | `TEACHER` | Grade submission; triggers Kafka notification to student |

### In-App Notifications (`/api/v1/notifications`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/notifications` | Authenticated | Get paginated list of recipient's notifications |
| `GET` | `/api/v1/notifications/unread-count` | Authenticated | Fetch total unread notifications count |
| `PATCH` | `/api/v1/notifications/{id}/read` | Authenticated | Mark specific notification as read |
| `POST` | `/api/v1/notifications/read-all` | Authenticated | Mark all notifications as read for current user |

### Dashboards & Operational Actuator
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/students/me/dashboard` | `STUDENT` | Summary of enrolled courses, progress, pending tasks |
| `GET` | `/api/v1/teacher/dashboard` | `TEACHER` | Stats on courses, enrollment counts, doubts, submissions |
| `GET` | `/api/v1/admin/dashboard` | `ADMIN` | System aggregate statistics and outbox event health |
| `GET` | `/actuator/health` | Public | Service health verification (MySQL, disk, liveness) |
| `GET` | `/swagger-ui.html` | Public | Interactive OpenAPI 3.0 documentation & testing UI |
