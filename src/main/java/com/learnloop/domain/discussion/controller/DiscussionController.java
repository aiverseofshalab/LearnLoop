package com.learnloop.domain.discussion.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.discussion.dto.QuestionDetailResponse;
import com.learnloop.domain.discussion.dto.QuestionRequest;
import com.learnloop.domain.discussion.dto.QuestionResponse;
import com.learnloop.domain.discussion.dto.ReplyRequest;
import com.learnloop.domain.discussion.dto.ReplyResponse;
import com.learnloop.domain.discussion.dto.UpdateQuestionStatusRequest;
import com.learnloop.domain.discussion.entity.QuestionStatus;
import com.learnloop.domain.discussion.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Discussions & Doubts", description = "Endpoints for asking questions, answering doubts, and tracking resolution")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @PostMapping
    @Operation(summary = "Ask a new doubt or academic question")
    public ResponseEntity<ApiResponse<QuestionResponse>> askQuestion(@Valid @RequestBody QuestionRequest request) {
        QuestionResponse question = discussionService.askQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Question posted successfully", question));
    }

    @GetMapping
    @Operation(summary = "Search and filter questions by course and status")
    public ResponseEntity<ApiResponse<Page<QuestionResponse>>> searchQuestions(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<QuestionResponse> questions = discussionService.searchQuestions(courseId, status, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question details along with all discussion replies")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionDetail(@PathVariable String id) {
        QuestionDetailResponse question = discussionService.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    @PostMapping("/{id}/replies")
    @Operation(summary = "Post a reply or answer to a doubt")
    public ResponseEntity<ApiResponse<ReplyResponse>> addReply(
            @PathVariable String id,
            @Valid @RequestBody ReplyRequest request) {
        ReplyResponse reply = discussionService.addReply(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reply added successfully", reply));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update question status (OPEN, ANSWERED, RESOLVED)")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateQuestionStatusRequest request) {
        QuestionResponse updated = discussionService.updateQuestionStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Question status updated", updated));
    }
}
