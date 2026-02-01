package com.smartchain.platform.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {
    private int unreadCount;
}
