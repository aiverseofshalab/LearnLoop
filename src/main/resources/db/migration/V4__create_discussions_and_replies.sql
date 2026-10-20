-- V4__create_discussions_and_replies.sql
-- Create questions and replies tables for LearnLoop (PostgreSQL)

CREATE TABLE IF NOT EXISTS questions (
    id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    lesson_id VARCHAR(36) NULL,
    student_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_questions PRIMARY KEY (id),
    CONSTRAINT fk_questions_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE SET NULL,
    CONSTRAINT fk_questions_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_course ON questions (course_id);
CREATE INDEX idx_questions_lesson ON questions (lesson_id);
CREATE INDEX idx_questions_student ON questions (student_id);
CREATE INDEX idx_questions_status ON questions (status);
CREATE INDEX idx_questions_created_at ON questions (created_at DESC);

CREATE TABLE IF NOT EXISTS replies (
    id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_teacher_reply BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_replies PRIMARY KEY (id),
    CONSTRAINT fk_replies_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_replies_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_replies_question ON replies (question_id);
CREATE INDEX idx_replies_user ON replies (user_id);
CREATE INDEX idx_replies_created_at ON replies (created_at ASC);
