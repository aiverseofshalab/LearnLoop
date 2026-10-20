package com.learnloop.domain.enrollment.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.common.exceptions.DuplicateResourceException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.audit.service.AuditService;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.repository.CourseRepository;
import com.learnloop.domain.enrollment.dto.EnrollmentResponse;
import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.kafka.event.EnrollmentEvent;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.repository.UserRepository;
import com.learnloop.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final OutboxService outboxService;
    private final AuditService auditService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             LessonRepository lessonRepository,
                             LessonProgressRepository lessonProgressRepository,
                             OutboxService outboxService,
                             AuditService auditService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.outboxService = outboxService;
        this.auditService = auditService;
    }

    @Transactional
    public EnrollmentResponse enrollInCourse(String courseId) {
        String studentId = SecurityUtils.getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("Cannot enroll in an unpublished course");
        }

        // Prevent teachers from enrolling in their own course
        if (course.getTeacher().getId().equals(studentId)) {
            throw new BadRequestException("Instructors cannot enroll in their own courses");
        }

        // Check duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("You are already enrolled in this course");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));

        Enrollment enrollment = new Enrollment(student, course);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // 1. Transactional Outbox Event for Kafka
        EnrollmentEvent event = new EnrollmentEvent(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                course.getId(),
                course.getTitle()
        );
        outboxService.saveEvent(KafkaConfig.TOPIC_ENROLLMENT, event);

        // 2. Audit Log
        auditService.logAction(
                studentId,
                "ENROLLMENT_CREATED",
                "COURSE",
                courseId,
                "Enrolled in course: " + course.getTitle(),
                "API"
        );

        log.info("Student {} successfully enrolled in course {}", studentId, courseId);

        long totalLessons = lessonRepository.countByCourseIdAndStatus(courseId, LessonStatus.PUBLISHED);
        return EnrollmentResponse.fromEntity(saved, 0.0, 0, totalLessons);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyEnrollments(Pageable pageable) {
        String studentId = SecurityUtils.getCurrentUserId();
        Page<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId, pageable);

        return enrollments.map(e -> {
            long totalLessons = lessonRepository.countByCourseIdAndStatus(e.getCourse().getId(), LessonStatus.PUBLISHED);
            long completed = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(e.getId());
            double percentage = totalLessons > 0 ? (completed * 100.0) / totalLessons : 0.0;
            return EnrollmentResponse.fromEntity(e, Math.round(percentage * 10.0) / 10.0, completed, totalLessons);
        });
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getTeacherCourseEnrollments(String courseId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId, pageable);
        return enrollments.map(e -> {
            long totalLessons = lessonRepository.countByCourseIdAndStatus(e.getCourse().getId(), LessonStatus.PUBLISHED);
            long completed = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(e.getId());
            double percentage = totalLessons > 0 ? (completed * 100.0) / totalLessons : 0.0;
            return EnrollmentResponse.fromEntity(e, Math.round(percentage * 10.0) / 10.0, completed, totalLessons);
        });
    }
}
