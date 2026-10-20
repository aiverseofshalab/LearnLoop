# LearnLoop Database Schema & Entity-Relationship Model (PostgreSQL)

## 1. Storage Engine & Dialect Specifications

LearnLoop uses **PostgreSQL (v16 LTS)** with the following specifications:
- **Engine**: PostgreSQL Relational Engine with full ACID transaction compliance.
- **Character Set**: `UTF8` with collation `en_US.UTF-8` or `C.UTF-8`.
- **Timezone**: All timestamps are stored as `TIMESTAMP` in Coordinated Universal Time (UTC).
- **Primary Identifiers**: Standardized UUIDv4 strings stored as `VARCHAR(36)`.
- **Numeric Precision**: Grading marks use `DECIMAL(5,2)` for exact fractional scores without floating-point inaccuracies.
- **Dialect**: `org.hibernate.dialect.PostgreSQLDialect`.

---

## 2. Mermaid Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ COURSES : "creates / owns"
    USERS ||--o{ ENROLLMENTS : "enrolls in"
    USERS ||--o{ LESSON_PROGRESS : "completes"
    USERS ||--o{ QUESTIONS : "asks"
    USERS ||--o{ REPLIES : "posts"
    USERS ||--o{ SUBMISSIONS : "submits"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ AUDIT_LOGS : "triggers"

    COURSES ||--o{ LESSONS : "contains"
    COURSES ||--o{ ENROLLMENTS : "has"
    COURSES ||--o{ QUESTIONS : "categorizes"
    COURSES ||--o{ ASSIGNMENTS : "specifies"

    LESSONS ||--o{ LESSON_PROGRESS : "tracks"
    LESSONS ||--o{ QUESTIONS : "references (optional)"

    QUESTIONS ||--o{ REPLIES : "contains"

    ASSIGNMENTS ||--o{ SUBMISSIONS : "receives"

    USERS {
        varchar(36) id PK
        varchar(100) full_name
        varchar(150) email UK
        varchar(255) password_hash
        varchar(20) role "STUDENT | TEACHER | ADMIN"
        varchar(20) status "ACTIVE | SUSPENDED"
        text bio
        varchar(500) avatar_url
        timestamp created_at
        timestamp updated_at
    }

    COURSES {
        varchar(36) id PK
        varchar(200) title
        varchar(500) short_description
        text detailed_description
        varchar(100) subject
        varchar(50) difficulty_level "BEGINNER | INTERMEDIATE | ADVANCED"
        varchar(36) teacher_id FK
        varchar(500) thumbnail_url
        varchar(20) status "DRAFT | PUBLISHED | ARCHIVED"
        timestamp created_at
        timestamp updated_at
    }

    LESSONS {
        varchar(36) id PK
        varchar(36) course_id FK
        varchar(200) title
        text description
        varchar(20) content_type "TEXT | VIDEO_LINK"
        text content_url_or_text
        int position
        varchar(20) status "DRAFT | PUBLISHED"
        timestamp created_at
        timestamp updated_at
    }

    ENROLLMENTS {
        varchar(36) id PK
        varchar(36) student_id FK
        varchar(36) course_id FK
        varchar(20) status "ACTIVE | COMPLETED | DROPPED"
        timestamp enrolled_at
        timestamp updated_at
    }

    LESSON_PROGRESS {
        varchar(36) id PK
        varchar(36) enrollment_id FK
        varchar(36) lesson_id FK
        varchar(36) student_id FK
        boolean completed
        timestamp completed_at
        timestamp updated_at
    }

    QUESTIONS {
        varchar(36) id PK
        varchar(36) course_id FK
        varchar(36) lesson_id FK "nullable"
        varchar(36) student_id FK
        varchar(255) title
        text description
        varchar(20) status "OPEN | ANSWERED | RESOLVED"
        timestamp created_at
        timestamp updated_at
    }

    REPLIES {
        varchar(36) id PK
        varchar(36) question_id FK
        varchar(36) user_id FK
        text content
        boolean is_teacher_reply
        timestamp created_at
        timestamp updated_at
    }

    ASSIGNMENTS {
        varchar(36) id PK
        varchar(36) course_id FK
        varchar(36) teacher_id FK
        varchar(255) title
        text instructions
        decimal(5_2) max_marks
        timestamp due_date
        varchar(20) status "PUBLISHED | CLOSED"
        timestamp created_at
        timestamp updated_at
    }

    SUBMISSIONS {
        varchar(36) id PK
        varchar(36) assignment_id FK
        varchar(36) student_id FK
        text text_content
        varchar(500) resource_url
        timestamp submitted_at
        varchar(20) status "SUBMITTED | GRADED"
        decimal(5_2) marks_obtained
        text feedback
        varchar(36) graded_by FK
        timestamp graded_at
        timestamp updated_at
    }

    NOTIFICATIONS {
        varchar(36) id PK
        varchar(36) user_id FK
        varchar(200) title
        text message
        varchar(50) type
        varchar(50) reference_type
        varchar(100) reference_id
        boolean is_read
        timestamp created_at
    }

    OUTBOX_EVENTS {
        varchar(36) id PK
        varchar(100) event_type
        varchar(100) topic
        varchar(100) aggregate_id
        text payload
        varchar(20) status "PENDING | PUBLISHED | FAILED"
        int retry_count
        text last_error
        timestamp created_at
        timestamp processed_at
    }

    PROCESSED_EVENTS {
        varchar(36) id PK
        varchar(100) event_id
        varchar(100) consumer_group
        varchar(100) event_type
        timestamp processed_at
    }

    AUDIT_LOGS {
        varchar(36) id PK
        varchar(36) user_id FK "nullable"
        varchar(100) action
        varchar(50) resource_type
        varchar(100) resource_id
        text details
        varchar(50) ip_address
        timestamp created_at
    }
```

---

## 3. Relational Constraints & Indexing Strategy

| Table | Constraint / Index Name | Columns | Purpose |
| :--- | :--- | :--- | :--- |
| `users` | `uq_users_email` | `email` (UNIQUE) | Prevents duplicate user accounts |
| `enrollments` | `uq_enrollments_student_course` | `student_id, course_id` (UNIQUE) | Guarantees single active enrollment per student |
| `lesson_progress` | `uq_progress_enrollment_lesson` | `enrollment_id, lesson_id` (UNIQUE) | Guarantees lesson completion idempotency |
| `submissions` | `uq_submissions_assignment_student` | `assignment_id, student_id` (UNIQUE) | Enforces single submission per assignment |
| `processed_events` | `uq_processed_events` | `event_id, consumer_group` (UNIQUE) | Prevents duplicate Kafka event side-effects |
| `notifications` | `idx_notifications_unread` | `user_id, is_read` | Speeds up unread notification counter & drawer queries |
| `lessons` | `idx_lessons_position` | `course_id, position ASC` | Efficient sequential syllabus loading |
| `questions` | `idx_questions_status` | `status` | Filtered doubt board retrieval |
| `outbox_events` | `idx_outbox_status` | `status, created_at ASC` | Fast background polling of pending events |
| `courses` | `idx_courses_status` | `status` | High-performance public course catalog search |
