package com.smartchain.platform.dto.notification;

import com.smartchain.platform.dto.review.common.PageDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private int unreadCount;
    private List<NotificationItemDto> content;
    private PageDto page;
}
