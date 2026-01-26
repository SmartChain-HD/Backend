package com.smartchain.platform.dto.management.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsResponse {
    private Map<String, Object> settings;
    private LocalDateTime lastUpdatedAt;
}
