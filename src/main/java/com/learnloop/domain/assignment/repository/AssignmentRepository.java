package com.learnloop.domain.assignment.repository;

import com.learnloop.domain.assignment.entity.Assignment;
import com.learnloop.domain.assignment.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findByCourseId(String courseId);

    List<Assignment> findByCourseIdAndStatus(String courseId, AssignmentStatus status);

    long countByTeacherId(String teacherId);

    long countByCourseId(String courseId);
}
