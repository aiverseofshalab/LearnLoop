-- V5__create_assignments_and_submissions.sql
-- Create assignments and submissions tables for LearnLoop (MySQL 8.4 LTS)

CREATE TABLE IF NOT EXISTS assignments (
    id CHAR(36) NOT NULL,
    course_id CHAR(36) NOT NULL,
    teacher_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    instructions TEXT NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    due_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_assignments PRIMARY KEY (id),
    CONSTRAINT fk_assignments_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_teacher FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_assignments_course ON assignments (course_id);
CREATE INDEX idx_assignments_teacher ON assignments (teacher_id);
CREATE INDEX idx_assignments_status ON assignments (status);
CREATE INDEX idx_assignments_due_date ON assignments (due_date);

CREATE TABLE IF NOT EXISTS submissions (
    id CHAR(36) NOT NULL,
    assignment_id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,
    text_content TEXT NOT NULL,
    resource_url VARCHAR(500) NULL,
    submitted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    marks_obtained DECIMAL(5,2) NULL,
    feedback TEXT NULL,
    graded_by CHAR(36) NULL,
    graded_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_submissions PRIMARY KEY (id),
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_grader FOREIGN KEY (graded_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uq_submissions_assignment_student UNIQUE (assignment_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_submissions_assignment ON submissions (assignment_id);
CREATE INDEX idx_submissions_student ON submissions (student_id);
CREATE INDEX idx_submissions_status ON submissions (status);
CREATE INDEX idx_submissions_graded_by ON submissions (graded_by);
