package com.learnloop.domain.course.service;

import com.learnloop.common.exceptions.ForbiddenException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.domain.course.dto.CourseDetailResponse;
import com.learnloop.domain.course.dto.CourseRequest;
import com.learnloop.domain.course.dto.CourseResponse;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.entity.DifficultyLevel;
import com.learnloop.domain.course.repository.CourseRepository;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.lesson.dto.LessonResponse;
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.service.UserService;
import com.learnloop.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserService userService;

    public CourseService(CourseRepository courseRepository,
                         LessonRepository lessonRepository,
                         EnrollmentRepository enrollmentRepository,
                         LessonProgressRepository lessonProgressRepository,
                         UserService userService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> searchPublishedCourses(String query, String subject, DifficultyLevel difficulty, Pageable pageable) {
        Page<Course> courses = courseRepository.searchPublishedCourses(query, subject, difficulty, pageable);
        return courses.map(c -> {
            long lessonCount = lessonRepository.countByCourseIdAndStatus(c.getId(), LessonStatus.PUBLISHED);
            return CourseResponse.fromEntity(c, lessonCount);
        });
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableSubjects() {
        return courseRepository.findDistinctPublishedSubjects();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        String currentUserId = SecurityUtils.getCurrentUser().map(u -> u.getId()).orElse(null);
        boolean isTeacher = currentUserId != null && course.getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);

        // Only published courses are visible to general public or students
        if (course.getStatus() != CourseStatus.PUBLISHED && !isTeacher && !isAdmin) {
            throw new ResourceNotFoundException("Course not found or is currently not published");
        }

        boolean isEnrolled = false;
        Set<String> completedLessonIds = Collections.emptySet();

        if (currentUserId != null) {
            isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(currentUserId, courseId);
            if (isEnrolled) {
                enrollmentRepository.findByStudentIdAndCourseId(currentUserId, courseId).ifPresent(enrollment -> {
                    completedLessonIds.addAll(lessonProgressRepository.findCompletedLessonIdsByEnrollmentId(enrollment.getId()));
                });
            }
        }

        // Fetch lessons
        List<Lesson> lessons = isTeacher || isAdmin
                ? lessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                : lessonRepository.findByCourseIdAndStatusOrderByPositionAsc(courseId, LessonStatus.PUBLISHED);

        boolean hasContentAccess = isEnrolled || isTeacher || isAdmin;
        final Set<String> finalCompleted = new HashSet<>(completedLessonIds);

        List<LessonResponse> lessonResponses = lessons.stream()
                .map(l -> LessonResponse.fromEntity(l, hasContentAccess, finalCompleted.contains(l.getId())))
                .toList();

        return CourseDetailResponse.fromEntity(course, isEnrolled, lessonResponses);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String teacherId = SecurityUtils.getCurrentUserId();
        User teacher = userService.getUserEntity(teacherId);

        Course course = new Course(
                request.getTitle().trim(),
                request.getShortDescription().trim(),
                request.getDetailedDescription().trim(),
                request.getSubject().trim(),
                request.getDifficultyLevel(),
                teacher,
                request.getThumbnailUrl()
        );

        Course saved = courseRepository.save(course);
        return CourseResponse.fromEntity(saved, 0);
    }

    @Transactional
    public CourseResponse updateCourse(String courseId, CourseRequest request) {
        Course course = getCourseWithOwnershipCheck(courseId);

        course.setTitle(request.getTitle().trim());
        course.setShortDescription(request.getShortDescription().trim());
        course.setDetailedDescription(request.getDetailedDescription().trim());
        course.setSubject(request.getSubject().trim());
        course.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getThumbnailUrl() != null) {
            course.setThumbnailUrl(request.getThumbnailUrl().trim());
        }

        Course saved = courseRepository.save(course);
        long lessonCount = lessonRepository.countByCourseId(courseId);
        return CourseResponse.fromEntity(saved, lessonCount);
    }

    @Transactional
    public CourseResponse publishCourse(String courseId) {
        Course course = getCourseWithOwnershipCheck(courseId);
        course.setStatus(CourseStatus.PUBLISHED);
        Course saved = courseRepository.save(course);
        long lessonCount = lessonRepository.countByCourseId(courseId);
        return CourseResponse.fromEntity(saved, lessonCount);
    }

    @Transactional
    public CourseResponse unpublishCourse(String courseId) {
        Course course = getCourseWithOwnershipCheck(courseId);
        course.setStatus(CourseStatus.DRAFT);
        Course saved = courseRepository.save(course);
        long lessonCount = lessonRepository.countByCourseId(courseId);
        return CourseResponse.fromEntity(saved, lessonCount);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getTeacherCourses(Pageable pageable) {
        String teacherId = SecurityUtils.getCurrentUserId();
        Page<Course> courses = courseRepository.findByTeacherId(teacherId, pageable);
        return courses.map(c -> {
            long lessonCount = lessonRepository.countByCourseId(c.getId());
            return CourseResponse.fromEntity(c, lessonCount);
        });
    }

    public Course getCourseWithOwnershipCheck(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = course.getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify this course");
        }

        return course;
    }

    @Transactional(readOnly = true)
    public Course getCourseEntity(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }
}
