# LearnLoop Automated Testing & Quality Assurance Guide

## 1. Test Suite Overview

LearnLoop includes a comprehensive automated test suite covering unit, business logic, security authorization, and event-processing behaviors.

### Test Categories
1. **Security & Authentication Tests**: Verifies BCrypt password hashing, token validation, duplicate email rejection, and strict rejection of unauthorized admin self-registration.
2. **Domain Service Tests**: Verifies course lifecycle, ownership rules, lesson content redaction, enrollment deduplication, doubt answering, assignment submissions, and grading constraints.
3. **Kafka & Outbox Reliability Tests**: Verifies transactional outbox record generation, outbox status transitions, Kafka message consumer handling, and consumer idempotency deduplication.

---

## 2. Test Execution Commands

### Run Full Test Suite (macOS / Linux)
```bash
./mvnw clean test
```

### Run Full Test Suite (Windows PowerShell)
```powershell
.\mvnw.cmd clean test
```

### Run a Specific Test Class
```bash
./mvnw test -Dtest=AuthServiceTest
```

### Run With Test Output Logging
```bash
./mvnw test -Dsurefire.useFile=false
```

---

## 3. Automated Test Coverage Matrix

| Test Class | Focus Area | Assertions & Behaviors Tested |
| :--- | :--- | :--- |
| `AuthServiceTest` | Authentication & Security | - Successful registration with BCrypt hashing<br/>- Rejection of duplicate email<br/>- Rejection of self-assigned `ADMIN` role<br/>- Successful login & JWT issuance |
| `CourseServiceTest` | Course Management | - Teacher course creation & draft status<br/>- Ownership check on updates<br/>- Unauthorized modification rejection<br/>- Content redaction for unenrolled students |
| `EnrollmentServiceTest` | Enrollments & Outbox | - Enrollment creation and progress initialization<br/>- Rejection of duplicate enrollment<br/>- Atomic outbox record dispatch |
| `DiscussionServiceTest` | Doubts & Q&A | - Student doubt creation<br/>- Teacher answer with verified instructor badge<br/>- Automatic question status transition to `ANSWERED` |
| `AssignmentServiceTest` | Assignments & Grading | - Student homework submission<br/>- Teacher grading with score validation (<= max marks)<br/>- Outbox grading event creation |
| `IdempotencyServiceTest` | Event Deduplication | - First-time event processing permitted<br/>- Duplicate event rejected via `processed_events` |
| `NotificationEventConsumerTest`| Kafka Consumer | - Enrollment event consumed -> In-app notification saved<br/>- Discussion reply event consumed -> Alert created for author |

---

## 4. Modern JVM & ByteBuddy Configuration

On modern JVMs (Java 21 to Java 25), ByteBuddy requires dynamic agent attachment flags. LearnLoop is pre-configured with:
- `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` in `maven-surefire-plugin`.
- Subclass MockMaker in `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`.

This ensures 100% clean test execution without JVM warnings or reflection security exceptions.
