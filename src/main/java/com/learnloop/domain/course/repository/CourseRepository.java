package com.learnloop.domain.course.repository;

import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.entity.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByTeacherId(String teacherId, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE " +
           "c.status = 'PUBLISHED' AND " +
           "(:query IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:subject IS NULL OR LOWER(c.subject) = LOWER(:subject)) AND " +
           "(:difficulty IS NULL OR c.difficultyLevel = :difficulty)")
    Page<Course> searchPublishedCourses(@Param("query") String query,
                                        @Param("subject") String subject,
                                        @Param("difficulty") DifficultyLevel difficulty,
                                        Pageable pageable);

    @Query("SELECT DISTINCT c.subject FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.subject ASC")
    List<String> findDistinctPublishedSubjects();

    long countByStatus(CourseStatus status);

    long countByTeacherId(String teacherId);

    long countByTeacherIdAndStatus(String teacherId, CourseStatus status);
}
