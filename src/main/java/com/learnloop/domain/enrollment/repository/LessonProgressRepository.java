package com.learnloop.domain.enrollment.repository;

import com.learnloop.domain.enrollment.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, String> {

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(String enrollmentId, String lessonId);

    List<LessonProgress> findByEnrollmentId(String enrollmentId);

    List<LessonProgress> findByStudentIdAndEnrollmentCourseId(String studentId, String courseId);

    long countByEnrollmentIdAndCompletedTrue(String enrollmentId);

    @Query("SELECT lp.lesson.id FROM LessonProgress lp WHERE lp.enrollment.id = :enrollmentId AND lp.completed = true")
    List<String> findCompletedLessonIdsByEnrollmentId(@Param("enrollmentId") String enrollmentId);

    long countByStudentIdAndCompletedTrue(String studentId);
}
