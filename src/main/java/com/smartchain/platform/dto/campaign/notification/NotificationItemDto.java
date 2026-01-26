package com.smartchain.platform.dto.campaign.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationItemDto {
    private Long notificationId;
    private String type;                 // APPROVAL_REQUEST, APPROVAL_COMPLETE, DEADLINE_REMINDER, REVISION_REQUEST
    private String typeLabel;
    private String typeIcon;             // 아이콘 URL
    private String title;
    private String message;
    private String linkUrl;              // 클릭시 이동할 URL
    private boolean isRead;
    private LocalDateTime createdAt;
    private String createdAtLabel;       // "2분 전", "어제"
}
