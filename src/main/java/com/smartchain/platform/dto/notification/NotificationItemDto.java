package com.smartchain.platform.dto.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationItemDto {
    private Long notificationId;
    private String type;
    private String title;
    private String message;
    private String linkUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}
