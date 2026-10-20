# LearnLoop Database Schema & Entity-Relationship Model (MySQL 8.4 LTS)

## 1. Storage Engine & Dialect Specifications

LearnLoop uses **MySQL 8.4 LTS** with the following database engine specifications:
- **Engine**: `InnoDB` with ACID transaction compliance.
- **Character Set**: `utf8mb4` with collation `utf8mb4_unicode_ci` (full multilingual & emoji support).
- **Timezone**: All timestamps are stored as `DATETIME(6)` in Coordinated Universal Time (UTC).
- **Primary Identifiers**: Standardized UUIDv4 strings stored as `CHAR(36)`.
- **Numeric Precision**: Grading marks use `DECIMAL(5,2)` for exact fractional scores without IEEE 754 floating-point inaccuracies.

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
        char(36) id PK
        varchar(120) full_name
        varchar(160) email UK
        varchar(255) password_hash
        varchar(20) role "STUDENT | TEACHER | ADMIN"
        varchar(20) status "ACTIVE | SUSPENDED"
        varchar(255) avatar_url
        datetime(6) created_at
        datetime(6) updated_at
    }

    COURSES {
        char(36) id PK
        varchar(200) title
        varchar(500) short_description
        mediumtext detailed_description
        varchar(80) subject
        varchar(30) difficulty_level "BEGINNER | INTERMEDIATE | ADVANCED"
        varchar(20) status "DRAFT | PUBLISHED | ARCHIVED"
        char(36) teacher_id FK
        varchar(500) thumbnail_url
        datetime(6) created_at
        datetime(6) updated_at
    }

    LESSONS {
        char(36) id PK
        char(36) course_id FK
        varchar(200) title
        varchar(500) description
        varchar(30) content_type "TEXT | VIDEO_LINK"
        mediumtext content_url_or_text
        int position
        varchar(20) status "DRAFT | PUBLISHED"
        datetime(6) created_at
        datetime(6) updated_at
    }

    ENROLLMENTS {
        char(36) id PK
        char(36) student_id FK
        char(36) course_id FK
        varchar(20) status "ACTIVE | COMPLETED | DROPPED"
        datetime(6) enrolled_at
        datetime(6) completed_at
    }

    LESSON_PROGRESS {
        char(36) id PK
        char(36) enrollment_id FK
        char(36) lesson_id FK
        char(36) student_id FK
        datetime(6) completed_at
    }

    QUESTIONS {
        char(36) id PK
        char(36) course_id FK
        char(36) lesson_id FK "nullable"
        char(36) author_id FK
        varchar(250) title
        mediumtext description
        varchar(20) status "OPEN | ANSWERED | RESOLVED"
        datetime(6) created_at
        datetime(6) updated_at
    }

    REPLIES {
        char(36) id PK
        char(36) question_id FK
        char(36) author_id FK
        mediumtext content
        boolean is_teacher_reply
        datetime(6) created_at
        datetime(6) updated_at
    }

    ASSIGNMENTS {
        char(36) id PK
        char(36) course_id FK
        char(36) teacher_id FK
        varchar(200) title
        mediumtext instructions
        decimal(5_2) max_marks
        datetime(6) due_date
        varchar(20) status "PUBLISHED | CLOSED"
        datetime(6) created_at
        datetime(6) updated_at
    }

    SUBMISSIONS {
        char(36) id PK
        char(36) assignment_id FK
        char(36) student_id FK
        mediumtext text_response
        varchar(500) external_resource_url
        datetime(6) submitted_at
        varchar(20) status "SUBMITTED | GRADED"
        decimal(5_2) marks_awarded
        mediumtext feedback
        datetime(6) graded_at
        char(36) graded_by_id FK
    }

    NOTIFICATIONS {
        char(36) id PK
        char(36) recipient_id FK
        varchar(200) title
        mediumtext message
        varchar(50) event_type
        varchar(500) reference_url
        boolean is_read
        datetime(6) created_at
        datetime(6) read_at
    }

    OUTBOX_EVENTS {
        char(36) id PK
        varchar(100) event_type
        varchar(100) topic
        varchar(36) aggregate_id
        mediumtext payload
        varchar(20) status "PENDING | PUBLISHED | FAILED"
        int retry_count
        mediumtext last_error
        datetime(6) created_at
        datetime(6) published_at
    }

    PROCESSED_EVENTS {
        varchar(100) event_id PK
        varchar(100) consumer_group PK
        datetime(6) processed_at
    }

    AUDIT_LOGS {
        char(36) id PK
        char(36) actor_id FK "nullable"
        varchar(100) action
        varchar(100) target_resource
        varchar(36) target_id
        varchar(45) ip_address
        datetime(6) created_at
    }
```

---

## 3. Relational Constraints & Indexing Strategy

| Table | Constraint / Index Name | Columns | Purpose |
| :--- | :--- | :--- | :--- |
| `users` | `uk_users_email` | `email` (UNIQUE) | Prevents duplicate user accounts |
| `enrollments` | `uk_enrollment_student_course` | `student_id, course_id` (UNIQUE) | Guarantees single active enrollment per student |
| `lesson_progress` | `uk_progress_enrollment_lesson` | `enrollment_id, lesson_id` (UNIQUE) | Guarantees lesson completion idempotency |
| `submissions` | `uk_submission_student_assignment` | `assignment_id, student_id` (UNIQUE) | Enforces single submission per assignment |
| `processed_events` | `PRIMARY KEY` | `event_id, consumer_group` | Prevents duplicate Kafka event side-effects |
| `notifications` | `idx_notif_recipient_read` | `recipient_id, is_read, created_at` | Speeds up unread notification counter & drawer queries |
| `lessons` | `idx_lessons_course_pos` | `course_id, position` | Efficient sequential syllabus loading |
| `questions` | `idx_questions_course_status` | `course_id, status, created_at` | Filtered doubt board retrieval |
| `outbox_events` | `idx_outbox_status_created` | `status, retry_count, created_at` | Fast background polling of pending events |
| `courses` | `idx_courses_status_cat` | `status, subject` | High-performance public course catalog search |
