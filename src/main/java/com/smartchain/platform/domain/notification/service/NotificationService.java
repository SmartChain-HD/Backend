package com.smartchain.platform.domain.notification.service;

import com.smartchain.platform.domain.notification.entity.Notification;
import com.smartchain.platform.domain.notification.repository.NotificationRepository;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.dto.notification.NotificationItemDto;
import com.smartchain.platform.dto.notification.NotificationListResponse;
import com.smartchain.platform.dto.notification.NotificationReadRequest;
import com.smartchain.platform.dto.notification.NotificationReadResponse;
import com.smartchain.platform.dto.review.common.PageDto;
import com.smartchain.platform.global.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications(Long userId, Boolean isRead, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByUserUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        int unreadCount = notificationRepository.countByUserUserIdAndIsRead(userId, false);

        List<NotificationItemDto> content = notificationPage.getContent().stream()
                .map(this::mapToItemDto)
                .toList();

        PageDto pageDto = PageDto.builder()
                .number(notificationPage.getNumber())
                .size(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .build();

        return NotificationListResponse.builder()
                .unreadCount(unreadCount)
                .content(content)
                .page(pageDto)
                .build();
    }

    @Transactional
    public NotificationReadResponse markAsRead(Long userId, NotificationReadRequest request) {
        int markedCount;

        if (Boolean.TRUE.equals(request.getMarkAllAsRead())) {
            markedCount = notificationRepository.markAllAsReadByUserId(userId);
            log.info("All notifications marked as read: userId={}, count={}", userId, markedCount);
        } else if (request.getNotificationIds() != null && !request.getNotificationIds().isEmpty()) {
            markedCount = notificationRepository.markAsReadByIds(request.getNotificationIds(), userId);
            log.info("Notifications marked as read: userId={}, ids={}, count={}",
                    userId, request.getNotificationIds(), markedCount);
        } else {
            markedCount = 0;
        }

        int remainingUnread = notificationRepository.countByUserUserIdAndIsRead(userId, false);

        return NotificationReadResponse.builder()
                .markedCount(markedCount)
                .remainingUnread(remainingUnread)
                .build();
    }

    @Transactional
    public Notification createNotification(User user, NotificationType type, String title, String message, String linkUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .linkUrl(linkUrl)
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: userId={}, type={}, title={}", user.getUserId(), type, title);
        return saved;
    }

    private NotificationItemDto mapToItemDto(Notification notification) {
        return NotificationItemDto.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .linkUrl(notification.getLinkUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
