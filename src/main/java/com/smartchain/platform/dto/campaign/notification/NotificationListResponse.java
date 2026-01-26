package com.smartchain.platform.dto.campaign.notification;

import com.smartchain.platform.dto.campaign.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private int unreadCount;             // v3.0: 순서 변경 (상단에 위치)
    private List<NotificationItemDto> content;  // v3.0: notifications → content
    private PageDto page;                // v3.0: 페이지네이션 표준화
}
