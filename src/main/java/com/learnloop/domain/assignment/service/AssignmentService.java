package com.learnloop.domain.assignment.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.common.exceptions.ForbiddenException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.assignment.dto.AssignmentRequest;
import com.learnloop.domain.assignment.dto.AssignmentResponse;
import com.learnloop.domain.assignment.dto.GradeSubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionResponse;
import com.learnloop.domain.assignment.entity.Assignment;
import com.learnloop.domain.assignment.entity.AssignmentStatus;
import com.learnloop.domain.assignment.entity.Submission;
import com.learnloop.domain.assignment.entity.SubmissionStatus;
import com.learnloop.domain.assignment.repository.AssignmentRepository;
import com.learnloop.domain.assignment.repository.SubmissionRepository;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.service.CourseService;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.kafka.event.AssignmentGradedEvent;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.service.UserService;
import com.learnloop.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final OutboxService outboxService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository,
                             CourseService courseService,
                             EnrollmentRepository enrollmentRepository,
                             UserService userService,
                             OutboxService outboxService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseService = courseService;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.outboxService = outboxService;
    }

    @Transactional
    public AssignmentResponse createAssignment(String courseId, AssignmentRequest request) {
        Course course = courseService.getCourseWithOwnershipCheck(courseId);
        String currentUserId = SecurityUtils.getCurrentUserId();
        User teacher = userService.getUserEntity(currentUserId);

        Assignment assignment = new Assignment(
                course,
                teacher,
                request.getTitle().trim(),
                request.getInstructions().trim(),
                request.getMaxMarks(),
                request.getDueDate()
        );

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Teacher {} created assignment {} for course {}", currentUserId, saved.getId(), courseId);
        return AssignmentResponse.fromEntity(saved, null);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getCourseAssignments(String courseId) {
        String currentUserId = SecurityUtils.getCurrentUser().map(u -> u.getId()).orElse(null);
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndStatus(courseId, AssignmentStatus.PUBLISHED);

        return assignments.stream()
                .map(a -> {
                    SubmissionResponse mySub = null;
                    if (currentUserId != null) {
                        mySub = submissionRepository.findByAssignmentIdAndStudentId(a.getId(), currentUserId)
                                .map(SubmissionResponse::fromEntity)
                                .orElse(null);
                    }
                    return AssignmentResponse.fromEntity(a, mySub);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        String currentUserId = SecurityUtils.getCurrentUser().map(u -> u.getId()).orElse(null);
        SubmissionResponse mySub = null;
        if (currentUserId != null) {
            mySub = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUserId)
                    .map(SubmissionResponse::fromEntity)
                    .orElse(null);
        }

        return AssignmentResponse.fromEntity(assignment, mySub);
    }

    @Transactional
    public SubmissionResponse submitAssignment(String assignmentId, SubmissionRequest request) {
        String studentId = SecurityUtils.getCurrentUserId();
        User student = userService.getUserEntity(studentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() == AssignmentStatus.CLOSED) {
            throw new BadRequestException("This assignment is closed for submissions");
        }

        // Verify enrollment
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, assignment.getCourse().getId())) {
            throw new ForbiddenException("You must be enrolled in this course to submit assignments");
        }

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(new Submission(assignment, student, request.getTextContent().trim(), request.getResourceUrl()));

        submission.setTextContent(request.getTextContent().trim());
        if (request.getResourceUrl() != null) {
            submission.setResourceUrl(request.getResourceUrl().trim());
        }

        // Handle resubmission state if already graded
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            submission.setStatus(SubmissionStatus.RESUBMITTED);
        } else {
            submission.setStatus(SubmissionStatus.SUBMITTED);
        }

        Submission saved = submissionRepository.save(submission);
        log.info("Student {} submitted assignment {}", studentId, assignmentId);
        return SubmissionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getTeacherSubmissions(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Enforce teacher course ownership
        courseService.getCourseWithOwnershipCheck(assignment.getCourse().getId());

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(SubmissionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public SubmissionResponse gradeSubmission(String submissionId, GradeSubmissionRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        Assignment assignment = submission.getAssignment();
        // Check teacher ownership
        courseService.getCourseWithOwnershipCheck(assignment.getCourse().getId());

        String teacherId = SecurityUtils.getCurrentUserId();
        User teacher = userService.getUserEntity(teacherId);

        if (request.getMarksObtained().compareTo(assignment.getMaxMarks()) > 0) {
            throw new BadRequestException("Marks obtained cannot exceed max marks of " + assignment.getMaxMarks());
        }

        submission.setMarksObtained(request.getMarksObtained());
        submission.setFeedback(request.getFeedback() != null ? request.getFeedback().trim() : null);
        submission.setGradedBy(teacher);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.GRADED);

        Submission saved = submissionRepository.save(submission);

        // Transactional Outbox Event for grading
        AssignmentGradedEvent event = new AssignmentGradedEvent(
                assignment.getId(),
                assignment.getTitle(),
                submission.getStudent().getId(),
                saved.getMarksObtained(),
                assignment.getMaxMarks(),
                teacher.getId(),
                assignment.getCourse().getId()
        );
        outboxService.saveEvent(KafkaConfig.TOPIC_ASSIGNMENT, event);

        log.info("Teacher {} graded submission {} for student {}", teacherId, submissionId, submission.getStudent().getId());
        return SubmissionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getMySubmissions(Pageable pageable) {
        String studentId = SecurityUtils.getCurrentUserId();
        return submissionRepository.findByStudentId(studentId, pageable)
                .map(SubmissionResponse::fromEntity);
    }
}
