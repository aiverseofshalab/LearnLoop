package com.learnloop.domain.assignment.repository;

import com.learnloop.domain.assignment.entity.Submission;
import com.learnloop.domain.assignment.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {

    List<Submission> findByAssignmentId(String assignmentId);

    Optional<Submission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);

    Page<Submission> findByStudentId(String studentId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.course.teacher.id = :teacherId AND s.status = 'SUBMITTED'")
    long countPendingSubmissionsForTeacher(@Param("teacherId") String teacherId);

    long countByStudentId(String studentId);
}
