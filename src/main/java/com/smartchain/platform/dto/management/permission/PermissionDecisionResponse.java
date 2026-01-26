package com.smartchain.platform.dto.management.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDecisionResponse {
    private Long accessRequestId;
    private String status;
    private LocalDateTime processedAt;
    private String message;
}
