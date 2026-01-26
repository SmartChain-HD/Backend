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
public class RoleHistoryItemDto {
    private String previousRole;
    private String newRole;
    private LocalDateTime changedAt;
    private String changedBy;
    private String changeReason;
}
