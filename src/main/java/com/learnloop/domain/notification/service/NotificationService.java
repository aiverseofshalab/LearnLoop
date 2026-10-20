package com.learnloop.domain.notification.service;

import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.domain.notification.dto.NotificationResponse;
import com.learnloop.domain.notification.dto.UnreadCountResponse;
import com.learnloop.domain.notification.entity.Notification;
import com.learnloop.domain.notification.entity.NotificationType;
import com.learnloop.domain.notification.repository.NotificationRepository;
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
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(String userId, String title, String message,
                                          NotificationType type, String refType, String refId) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Notification notification = new Notification(recipient, title, message, type, refType, refId);
        Notification saved = notificationRepository.save(notification);
        log.info("Created in-app notification {} for user {}", saved.getId(), userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getMyUnreadCount() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        long count = notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found or access denied"));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Transactional
    public void markAllAsRead() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        int updatedCount = notificationRepository.markAllAsReadForUser(currentUserId);
        log.info("Marked {} notifications as read for user {}", updatedCount, currentUserId);
    }
}
