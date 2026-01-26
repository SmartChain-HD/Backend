package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMetadataDto {
    private String ipAddress;
    private String userAgent;
}
