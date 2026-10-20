package com.learnloop.domain.discussion.repository;

import com.learnloop.domain.discussion.entity.Question;
import com.learnloop.domain.discussion.entity.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    @Query("SELECT q FROM Question q JOIN FETCH q.student JOIN FETCH q.course WHERE " +
           "(:courseId IS NULL OR q.course.id = :courseId) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:query IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Question> searchQuestions(@Param("courseId") String courseId,
                                   @Param("status") QuestionStatus status,
                                   @Param("query") String query,
                                   Pageable pageable);

    Page<Question> findByStudentId(String studentId, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.course.teacher.id = :teacherId")
    Page<Question> findByCourseTeacherId(@Param("teacherId") String teacherId, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.course.teacher.id = :teacherId AND q.status = 'OPEN'")
    long countUnansweredQuestionsForTeacher(@Param("teacherId") String teacherId);

    long countByStatus(QuestionStatus status);
}
