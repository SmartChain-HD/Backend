package com.smartchain.platform.dto.management.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingUpdateRequest {
    @NotBlank
    private String settingKey;
    
    @NotNull
    private Object settingValue;
    
    private String reason;
}
