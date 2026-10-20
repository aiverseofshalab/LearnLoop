package com.learnloop.domain.lesson.repository;

import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {

    List<Lesson> findByCourseIdOrderByPositionAsc(String courseId);

    List<Lesson> findByCourseIdAndStatusOrderByPositionAsc(String courseId, LessonStatus status);

    long countByCourseId(String courseId);

    long countByCourseIdAndStatus(String courseId, LessonStatus status);

    @Query("SELECT COALESCE(MAX(l.position), 0) FROM Lesson l WHERE l.course.id = :courseId")
    int findMaxPositionByCourseId(@Param("courseId") String courseId);

    Optional<Lesson> findByIdAndCourseId(String id, String courseId);
}
