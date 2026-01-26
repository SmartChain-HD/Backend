package com.smartchain.platform.dto.campaign.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypeSettingDto {
    private String typeCode;
    private String typeName;
    private boolean emailEnabled;
    private boolean pushEnabled;
}
