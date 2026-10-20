package com.learnloop.domain.enrollment.service;

import com.learnloop.domain.assignment.entity.SubmissionStatus;
import com.learnloop.domain.assignment.repository.AssignmentRepository;
import com.learnloop.domain.assignment.repository.SubmissionRepository;
import com.learnloop.domain.course.dto.CourseResponse;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.repository.CourseRepository;
import com.learnloop.domain.discussion.repository.QuestionRepository;
import com.learnloop.domain.enrollment.dto.AdminDashboardResponse;
import com.learnloop.domain.enrollment.dto.EnrollmentResponse;
import com.learnloop.domain.enrollment.dto.StudentDashboardResponse;
import com.learnloop.domain.enrollment.dto.TeacherDashboardResponse;
import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.notification.repository.NotificationRepository;
import com.learnloop.domain.outbox.entity.OutboxStatus;
import com.learnloop.domain.outbox.repository.OutboxEventRepository;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.repository.UserRepository;
import com.learnloop.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;

    public DashboardService(EnrollmentRepository enrollmentRepository,
                            CourseRepository courseRepository,
                            LessonRepository lessonRepository,
                            LessonProgressRepository lessonProgressRepository,
                            QuestionRepository questionRepository,
                            SubmissionRepository submissionRepository,
                            NotificationRepository notificationRepository,
                            UserRepository userRepository,
                            OutboxEventRepository outboxEventRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard() {
        String studentId = SecurityUtils.getCurrentUserId();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId, PageRequest.of(0, 100)).getContent();

        List<EnrollmentResponse> enrolledCourseResponses = enrollments.stream().map(e -> {
            long totalLessons = lessonRepository.countByCourseIdAndStatus(e.getCourse().getId(), LessonStatus.PUBLISHED);
            long completed = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(e.getId());
            double percentage = totalLessons > 0 ? (completed * 100.0) / totalLessons : 0.0;
            return EnrollmentResponse.fromEntity(e, Math.round(percentage * 10.0) / 10.0, completed, totalLessons);
        }).toList();

        long totalCompleted = lessonProgressRepository.countByStudentIdAndCompletedTrue(studentId);
        long unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(studentId);

        double totalPercentage = enrolledCourseResponses.stream()
                .mapToDouble(EnrollmentResponse::getProgressPercentage)
                .average()
                .orElse(0.0);

        StudentDashboardResponse response = new StudentDashboardResponse();
        response.setEnrolledCourses(enrolledCourseResponses);
        response.setTotalEnrolledCourses(enrolledCourseResponses.size());
        response.setTotalCompletedLessons(totalCompleted);
        response.setOverallProgressPercentage(Math.round(totalPercentage * 10.0) / 10.0);
        response.setUnreadNotificationsCount(unreadNotifications);

        return response;
    }

    @Transactional(readOnly = true)
    public TeacherDashboardResponse getTeacherDashboard() {
        String teacherId = SecurityUtils.getCurrentUserId();

        long totalCourses = courseRepository.countByTeacherId(teacherId);
        long publishedCourses = courseRepository.countByTeacherIdAndStatus(teacherId, CourseStatus.PUBLISHED);
        long draftCourses = courseRepository.countByTeacherIdAndStatus(teacherId, CourseStatus.DRAFT);
        long totalEnrollments = enrollmentRepository.countTotalEnrollmentsForTeacher(teacherId);
        long unansweredQuestions = questionRepository.countUnansweredQuestionsForTeacher(teacherId);
        long pendingSubmissions = submissionRepository.countPendingSubmissionsForTeacher(teacherId);

        List<Course> recentCourseEntities = courseRepository.findByTeacherId(
                teacherId, PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent();

        List<CourseResponse> recentCourses = recentCourseEntities.stream().map(c -> {
            long lessonCount = lessonRepository.countByCourseId(c.getId());
            return CourseResponse.fromEntity(c, lessonCount);
        }).toList();

        TeacherDashboardResponse response = new TeacherDashboardResponse();
        response.setTotalCourses(totalCourses);
        response.setPublishedCourses(publishedCourses);
        response.setDraftCourses(draftCourses);
        response.setTotalEnrollments(totalEnrollments);
        response.setUnansweredQuestionsCount(unansweredQuestions);
        response.setPendingSubmissionsCount(pendingSubmissions);
        response.setRecentCourses(recentCourses);

        return response;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setTotalUsers(userRepository.count());
        response.setTotalStudents(userRepository.countByRole(UserRole.STUDENT));
        response.setTotalTeachers(userRepository.countByRole(UserRole.TEACHER));
        response.setTotalCourses(courseRepository.count());
        response.setPublishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED));
        response.setTotalEnrollments(enrollmentRepository.count());
        response.setPendingOutboxEvents(outboxEventRepository.countByStatus(OutboxStatus.PENDING));
        return response;
    }
}
