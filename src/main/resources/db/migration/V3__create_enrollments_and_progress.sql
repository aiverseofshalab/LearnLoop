-- V3__create_enrollments_and_progress.sql
-- Create enrollments and lesson progress tables for LearnLoop (PostgreSQL)

CREATE TABLE IF NOT EXISTS enrollments (
    id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX idx_enrollments_student ON enrollments (student_id);
CREATE INDEX idx_enrollments_course ON enrollments (course_id);
CREATE INDEX idx_enrollments_status ON enrollments (status);

CREATE TABLE IF NOT EXISTS lesson_progress (
    id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    lesson_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_lesson_progress PRIMARY KEY (id),
    CONSTRAINT fk_progress_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_progress_enrollment_lesson UNIQUE (enrollment_id, lesson_id)
);

CREATE INDEX idx_progress_student ON lesson_progress (student_id);
CREATE INDEX idx_progress_lesson ON lesson_progress (lesson_id);
CREATE INDEX idx_progress_completed ON lesson_progress (completed);
