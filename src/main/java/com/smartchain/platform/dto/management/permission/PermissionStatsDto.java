package com.smartchain.platform.dto.management.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStatsDto {
    private int totalPending;            // 대기중
    private int totalApproved;           // 승인됨
    private int totalRejected;           // 반려됨
}
