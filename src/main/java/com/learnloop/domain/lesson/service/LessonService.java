package com.learnloop.domain.lesson.service;

import com.learnloop.common.exceptions.ForbiddenException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.service.CourseService;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.lesson.dto.LessonRequest;
import com.learnloop.domain.lesson.dto.LessonResponse;
import com.learnloop.domain.lesson.dto.ReorderLessonsRequest;
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public LessonService(LessonRepository lessonRepository,
                         CourseService courseService,
                         EnrollmentRepository enrollmentRepository,
                         LessonProgressRepository lessonProgressRepository) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsForCourse(String courseId) {
        String currentUserId = SecurityUtils.getCurrentUser().map(u -> u.getId()).orElse(null);
        Course course = courseService.getCourseEntity(courseId);

        boolean isTeacher = currentUserId != null && course.getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);
        boolean isEnrolled = currentUserId != null && enrollmentRepository.existsByStudentIdAndCourseId(currentUserId, courseId);

        if (!isTeacher && !isAdmin && !isEnrolled) {
            throw new ForbiddenException("You must be enrolled in this course to access its lessons");
        }

        List<Lesson> lessons = isTeacher || isAdmin
                ? lessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                : lessonRepository.findByCourseIdAndStatusOrderByPositionAsc(courseId, LessonStatus.PUBLISHED);

        Set<String> completedIds = new HashSet<>();
        if (isEnrolled) {
            enrollmentRepository.findByStudentIdAndCourseId(currentUserId, courseId).ifPresent(enrollment -> {
                completedIds.addAll(lessonProgressRepository.findCompletedLessonIdsByEnrollmentId(enrollment.getId()));
            });
        }

        return lessons.stream()
                .map(l -> LessonResponse.fromEntity(l, true, completedIds.contains(l.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonById(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        String currentUserId = SecurityUtils.getCurrentUserId();
        Course course = lesson.getCourse();

        boolean isTeacher = course.getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(currentUserId, course.getId());

        if (!isTeacher && !isAdmin && !isEnrolled) {
            throw new ForbiddenException("You do not have permission to access this lesson");
        }

        boolean completed = false;
        if (isEnrolled) {
            var enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUserId, course.getId());
            if (enrollment.isPresent()) {
                var progress = lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollment.get().getId(), lessonId);
                completed = progress.map(p -> p.isCompleted()).orElse(false);
            }
        }

        return LessonResponse.fromEntity(lesson, true, completed);
    }

    @Transactional
    public LessonResponse addLesson(String courseId, LessonRequest request) {
        Course course = courseService.getCourseWithOwnershipCheck(courseId);

        int nextPosition = request.getPosition() != null && request.getPosition() > 0
                ? request.getPosition()
                : lessonRepository.findMaxPositionByCourseId(courseId) + 1;

        Lesson lesson = new Lesson(
                course,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getContentType(),
                request.getContentUrlOrText().trim(),
                nextPosition
        );
        if (request.getStatus() != null) {
            lesson.setStatus(request.getStatus());
        }

        Lesson saved = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(saved, true, false);
    }

    @Transactional
    public LessonResponse updateLesson(String lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Enforce ownership via course
        courseService.getCourseWithOwnershipCheck(lesson.getCourse().getId());

        lesson.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription().trim());
        }
        lesson.setContentType(request.getContentType());
        lesson.setContentUrlOrText(request.getContentUrlOrText().trim());
        if (request.getPosition() != null && request.getPosition() > 0) {
            lesson.setPosition(request.getPosition());
        }
        if (request.getStatus() != null) {
            lesson.setStatus(request.getStatus());
        }

        Lesson saved = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(saved, true, false);
    }

    @Transactional
    public void deleteLesson(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        courseService.getCourseWithOwnershipCheck(lesson.getCourse().getId());
        lessonRepository.delete(lesson);
    }

    @Transactional
    public List<LessonResponse> reorderLessons(String courseId, ReorderLessonsRequest request) {
        courseService.getCourseWithOwnershipCheck(courseId);

        int position = 1;
        for (String lessonId : request.getLessonIds()) {
            var lessonOpt = lessonRepository.findByIdAndCourseId(lessonId, courseId);
            if (lessonOpt.isPresent()) {
                Lesson lesson = lessonOpt.get();
                lesson.setPosition(position++);
                lessonRepository.save(lesson);
            }
        }

        List<Lesson> updatedList = lessonRepository.findByCourseIdOrderByPositionAsc(courseId);
        return updatedList.stream()
                .map(l -> LessonResponse.fromEntity(l, true, false))
                .toList();
    }
}
