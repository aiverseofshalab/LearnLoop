package com.learnloop.domain.notification.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.notification.dto.NotificationResponse;
import com.learnloop.domain.notification.dto.UnreadCountResponse;
import com.learnloop.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Endpoints for user notifications and unread badges")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get paginated notifications for the current authenticated user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count for badge indicators")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UnreadCountResponse count = notificationService.getMyUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable String id) {
        NotificationResponse updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.successMessage("All notifications marked as read"));
    }
}
