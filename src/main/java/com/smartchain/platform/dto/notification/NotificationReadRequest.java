package com.smartchain.platform.dto.notification;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationReadRequest {
    private List<Long> notificationIds;
    private Boolean markAllAsRead;
}
