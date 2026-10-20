package com.learnloop.domain.enrollment.service;

import com.learnloop.common.exceptions.ForbiddenException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.enrollment.dto.StudentProgressResponse;
import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.entity.LessonProgress;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.kafka.event.LessonCompletedEvent;
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final OutboxService outboxService;

    public ProgressService(LessonRepository lessonRepository,
                           EnrollmentRepository enrollmentRepository,
                           LessonProgressRepository lessonProgressRepository,
                           OutboxService outboxService) {
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public StudentProgressResponse markLessonComplete(String lessonId) {
        String studentId = SecurityUtils.getCurrentUserId();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        String courseId = lesson.getCourse().getId();

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ForbiddenException("You must be enrolled in this course to track progress"));

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElseGet(() -> new LessonProgress(enrollment, lesson, enrollment.getStudent(), false));

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            lessonProgressRepository.save(progress);

            // Publish Outbox event for learning activity
            LessonCompletedEvent event = new LessonCompletedEvent(
                    studentId,
                    lesson.getId(),
                    lesson.getTitle(),
                    courseId,
                    lesson.getCourse().getTitle()
            );
            outboxService.saveEvent(KafkaConfig.TOPIC_LEARNING_ACTIVITY, event);

            log.info("Student {} marked lesson {} complete in course {}", studentId, lessonId, courseId);
        }

        return calculateProgress(enrollment);
    }

    @Transactional(readOnly = true)
    public StudentProgressResponse getCourseProgress(String courseId) {
        String studentId = SecurityUtils.getCurrentUserId();

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ForbiddenException("You are not enrolled in this course"));

        return calculateProgress(enrollment);
    }

    private StudentProgressResponse calculateProgress(Enrollment enrollment) {
        String courseId = enrollment.getCourse().getId();
        long totalLessons = lessonRepository.countByCourseIdAndStatus(courseId, LessonStatus.PUBLISHED);
        long completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        double percentage = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;
        double rounded = Math.round(percentage * 10.0) / 10.0;

        return new StudentProgressResponse(
                courseId,
                enrollment.getCourse().getTitle(),
                completedLessons,
                totalLessons,
                rounded
        );
    }
}
