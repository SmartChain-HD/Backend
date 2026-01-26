package com.smartchain.platform.dto.campaign.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadRequest {
    private List<Long> notificationIds;  // 비어있으면 전체 읽음 처리
    private Boolean markAllAsRead;       // v3.0: 전체 읽음 처리 플래그 추가
}
