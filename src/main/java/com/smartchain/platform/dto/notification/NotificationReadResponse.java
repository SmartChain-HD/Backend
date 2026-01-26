package com.smartchain.platform.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadResponse {
    private int markedCount;
    private int remainingUnread;
}
