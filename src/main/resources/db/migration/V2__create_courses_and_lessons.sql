-- V2__create_courses_and_lessons.sql
-- Create courses and lessons tables for LearnLoop (PostgreSQL)

CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    detailed_description TEXT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL DEFAULT 'BEGINNER',
    teacher_id VARCHAR(36) NOT NULL,
    thumbnail_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_courses_status ON courses (status);
CREATE INDEX idx_courses_subject ON courses (subject);
CREATE INDEX idx_courses_difficulty ON courses (difficulty_level);
CREATE INDEX idx_courses_teacher ON courses (teacher_id);
CREATE INDEX idx_courses_created_at ON courses (created_at DESC);

CREATE TABLE IF NOT EXISTS lessons (
    id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    content_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content_url_or_text TEXT NOT NULL,
    position INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_lessons PRIMARY KEY (id),
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

CREATE INDEX idx_lessons_course_id ON lessons (course_id);
CREATE INDEX idx_lessons_position ON lessons (course_id, position ASC);
CREATE INDEX idx_lessons_status ON lessons (status);
