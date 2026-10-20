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
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.service.UserService;
import com.learnloop.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private User teacher;
    private User otherTeacher;
    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = new User("teacher@learnloop.com", "hash", "Prof. Alan", UserRole.TEACHER);
        otherTeacher = new User("other@learnloop.com", "hash", "Prof. Bob", UserRole.TEACHER);
        student = new User("student@learnloop.com", "hash", "Charlie", UserRole.STUDENT);

        course = new Course("Spring Boot in Action", "Short desc", "Detailed desc",
                "Backend", DifficultyLevel.INTERMEDIATE, teacher, "https://example.com/img.jpg");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Teacher creates course in DRAFT status")
    void createCourse_Success() {
        UserPrincipal principal = UserPrincipal.create(teacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        CourseRequest request = new CourseRequest("Spring Boot in Action", "Short desc", "Detailed desc",
                "Backend", DifficultyLevel.INTERMEDIATE, "https://example.com/img.jpg");

        when(userService.getUserEntity(teacher.getId())).thenReturn(teacher);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.createCourse(request);

        assertNotNull(response);
        assertEquals(CourseStatus.DRAFT, response.getStatus());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Teacher publishes their own course")
    void publishCourse_Success() {
        UserPrincipal principal = UserPrincipal.create(teacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.publishCourse(course.getId());

        assertNotNull(response);
        assertEquals(CourseStatus.PUBLISHED, response.getStatus());
    }

    @Test
    @DisplayName("Teacher attempting to modify another teacher's course throws ForbiddenException")
    void updateCourse_ByOtherTeacher_ThrowsForbidden() {
        UserPrincipal otherPrincipal = UserPrincipal.create(otherTeacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherPrincipal, null, otherPrincipal.getAuthorities()));

        CourseRequest request = new CourseRequest("Hacked Title", "Short", "Detailed", "Backend", DifficultyLevel.BEGINNER, null);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ForbiddenException.class, () -> courseService.updateCourse(course.getId(), request));
    }

    @Test
    @DisplayName("Non-enrolled student viewing course detail receives syllabus with redacted lesson content")
    void getCourseDetail_NonEnrolled_RedactsLessonContent() {
        UserPrincipal studentPrincipal = UserPrincipal.create(student);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(studentPrincipal, null, studentPrincipal.getAuthorities()));

        course.setStatus(CourseStatus.PUBLISHED);
        Lesson lesson = new Lesson(course, "Introduction", "Overview", com.learnloop.domain.lesson.entity.ContentType.TEXT, "Secret Lesson Content", 1);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())).thenReturn(false);
        when(lessonRepository.findByCourseIdAndStatusOrderByPositionAsc(course.getId(), LessonStatus.PUBLISHED))
                .thenReturn(List.of(lesson));

        CourseDetailResponse response = courseService.getCourseDetail(course.getId());

        assertNotNull(response);
        assertEquals(1, response.getLessons().size());
        assertEquals("Introduction", response.getLessons().get(0).getTitle());
        // Crucial security check: private lesson content MUST be null/redacted for non-enrolled students!
        assertNull(response.getLessons().get(0).getContentUrlOrText());
    }
}
