package com.learnloop.domain.enrollment.repository;

import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    Optional<Enrollment> findByStudentIdAndCourseId(String studentId, String courseId);

    boolean existsByStudentIdAndCourseId(String studentId, String courseId);

    Page<Enrollment> findByStudentId(String studentId, Pageable pageable);

    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    Page<Enrollment> findByCourseId(String courseId, Pageable pageable);

    long countByCourseId(String courseId);

    long countByStudentId(String studentId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.teacher.id = :teacherId")
    long countTotalEnrollmentsForTeacher(@Param("teacherId") String teacherId);
}
