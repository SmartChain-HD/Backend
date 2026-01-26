package com.smartchain.platform.dto.campaign.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadResponse {
    private int markedCount;             // v3.0: 처리된 알림 수
    private int remainingUnread;         // v3.0: 남은 미읽음 수
}
