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
public class NotificationSettingsResponse {
    private boolean emailEnabled;
    private boolean pushEnabled;
    private List<NotificationTypeSettingDto> typeSettings;
}
