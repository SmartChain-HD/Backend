package com.smartchain.platform.dto.management.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementStatsDto {
    private int totalUsers;              // 전체 사용자
    private int activeUsers;             // 활성 사용자
    private int inactiveUsers;           // 비활성 사용자
    private int recentLogins7d;          // v3.0: recentLogins → recentLogins7d (명확화)
}
