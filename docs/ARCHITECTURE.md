# LearnLoop Architecture Documentation

## 1. Architectural Overview & Style

**LearnLoop** is architected as a **Modular Monolith** built on **Spring Boot 3.3.5** and **Java 21 LTS**, with an asynchronous event-driven subsystem powered by **Apache Kafka** using the **Transactional Outbox Pattern**.

The primary data store is **PostgreSQL (v16 LTS)**, managed via strict, versioned **Flyway** migrations.

```mermaid
graph TD
    subgraph ClientLayer ["Client Layer (Cross-Platform)"]
        Browser["Desktop & Mobile Browsers / PWA"]
        SW["Service Worker (Asset Cache & Offline Fallback)"]
    end

    subgraph SecurityGateway ["Security & HTTP Gateway"]
        SecurityFilter["Spring Security Filter Chain"]
        JWT["Stateless JWT / HttpOnly Cookie Validation"]
        RateLimit["Rate Limiting & Correlation ID Filter"]
    end

    subgraph ModularMonolith ["Spring Boot 3.3.5 Modular Monolith"]
        UserMod["User & Auth Module<br/>(Registration, Profiles, Admin Bootstrap)"]
        CourseMod["Course & Lesson Module<br/>(Ownership, Redaction, Ordering)"]
        EnrollMod["Enrollment & Progress Module<br/>(Deduplication, Completion Math)"]
        DiscussMod["Discussion & Doubt Module<br/>(Questions, Instructor Replies, Badges)"]
        AssignMod["Assignment & Grading Module<br/>(Submissions, Scores, Feedback)"]
        NotifMod["Notification Module<br/>(In-App Alerts, Unread Counters)"]
        OutboxMod["Transactional Outbox Engine<br/>(Atomic Commits, Scheduled Poller)"]
    end

    subgraph Persistence ["Relational Persistence (PostgreSQL 16 LTS)"]
        DB[(PostgreSQL 16 LTS<br/>UTF-8 Encoding<br/>ACID Transactions)]
    end

    subgraph Messaging ["Event Streaming (Apache Kafka KRaft)"]
        Broker["Kafka Cluster (KRaft Mode)"]
        Topics["Event Topics:<br/>- learnloop.enrollment.events<br/>- learnloop.discussion.events<br/>- learnloop.assignment.events<br/>- learnloop.learning-activity.events<br/>- learnloop.dlt"]
    end

    Browser -->|HTTPS REST JSON| SecurityFilter
    SecurityFilter --> JWT
    JWT --> UserMod & CourseMod & EnrollMod & DiscussMod & AssignMod & NotifMod
    
    UserMod & CourseMod & EnrollMod & DiscussMod & AssignMod & NotifMod -->|Spring Data JPA / HikariCP| DB
    CourseMod & EnrollMod & DiscussMod & AssignMod -->|Write Outbox Record (Same Tx)| OutboxMod
    OutboxMod --> DB

    OutboxMod -.->|Poll Unsent & Publish| Topics
    Topics -.->|Consume & Idempotent Deduplicate| NotifMod
```

---

## 2. Rationale: Why a Modular Monolith?

1. **Single-Developer Maintainability**:
   - Avoids distributed transaction overhead (2PC, complex sagas, network partition failures).
   - Eliminates cascading failures, complex orchestration meshes, and inter-service network latency.
2. **Strict In-Process Module Boundaries**:
   - Each domain package (`user`, `course`, `lesson`, `enrollment`, `discussion`, `assignment`, `notification`, `outbox`) encapsulates its repositories, entities, services, and DTOs.
   - Cross-domain interactions occur strictly through explicit service interfaces or asynchronous domain events.
3. **Decoupled Asynchronous Processing**:
   - Time-insensitive side-effects (such as dispatching in-app alerts, generating audit trails, and aggregating activity metrics) are offloaded to Kafka.
   - HTTP response latency remains under 30ms for write operations because notifications are processed asynchronously without blocking the client.

---

## 3. Package Organization

The backend source code under `src/main/java/com/learnloop/` follows domain-driven packaging:

```text
com.learnloop
├── LearnLoopApplication.java           # Spring Boot Bootstrap & Scheduling Enablement
├── common/                             # Shared contracts & infrastructure
│   ├── ApiResponse.java                # Unified REST response wrapper
│   ├── ErrorResponse.java              # Standardized RFC 7807-style error structure
│   ├── GlobalExceptionHandler.java     # Centralized @ControllerAdvice
│   ├── CorrelationIdFilter.java        # Request tracing (X-Correlation-ID)
│   └── exception/                      # Domain exceptions (NotFound, Unauthorized, Conflict)
├── config/                             # Spring Configuration
│   ├── SecurityConfig.java             # Spring Security, CORS, stateless session, filters
│   ├── KafkaConfig.java                # Kafka producer, consumer, topic definitions, DLT
│   └── OpenApiConfig.java              # OpenAPI 3.0 / Swagger UI documentation
├── security/                           # Authentication internals
│   ├── JwtTokenProvider.java           # HMAC-SHA256 signature, parsing, claims
│   ├── JwtAuthenticationFilter.java    # Bearer header & HttpOnly cookie extraction
│   ├── UserPrincipal.java              # Spring Security UserDetails adapter
│   └── CustomUserDetailsService.java   # Database user loading
├── domain/                             # Modular business domains
│   ├── user/                           # Registration, Auth, Admin Management
│   ├── course/                         # Course lifecycle, publication, ownership
│   ├── lesson/                         # Lesson ordering, video/text, access control
│   ├── enrollment/                     # Course enrollment, progress denominator math
│   ├── discussion/                     # Doubts, Q&A, instructor responses
│   ├── assignment/                     # Homework, submissions, grading
│   ├── notification/                   # In-app notifications & read markers
│   ├── outbox/                         # Transactional outbox & consumer idempotency
│   ├── kafka/                          # Event contracts, producers, and consumers
│   └── audit/                          # Administrative audit logging
```

---

## 4. Transaction Boundaries & Event Consistency

To guarantee data consistency between PostgreSQL and Kafka without distributed 2-phase commits, LearnLoop uses the **Transactional Outbox Pattern**:

1. **Business Operation**: A state-changing operation (e.g. `EnrollmentService.enroll()`) begins a `@Transactional` boundary in PostgreSQL.
2. **Business Record Insert**: The entity record (e.g., `Enrollment`) is written to the database.
3. **Outbox Record Insert**: In the **exact same database transaction**, an `OutboxEvent` record is saved to the `outbox_events` table with status `PENDING`.
4. **Transaction Commit**: PostgreSQL commits both records atomically. If the database fails, both rollback.
5. **Scheduled Publisher**: An autonomous background worker (`OutboxPublisher`) polls the `outbox_events` table for `PENDING` records and dispatches them to Apache Kafka.
6. **Delivery Confirmation**: Upon Kafka broker ACK, the outbox record is marked `PUBLISHED`. If broker publishing fails, retry counters increment with exponential backoff until maximum retries, after which the event is routed to Dead-Letter storage (`learnloop.dlt`).
7. **Idempotent Consumption**: Consumers check the `processed_events` table before applying side-effects, guaranteeing that duplicate Kafka deliveries (due to network retries) produce no duplicate records.
