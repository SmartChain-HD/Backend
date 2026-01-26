package com.smartchain.platform.dto.campaign.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardInfoDto {
    private String userName;
    /**
     * 역할
     * 
     * ⚠️ v3.0 변경: ADMIN 역할 제거
     * 가능한 값: GUEST, DRAFTER, APPROVER, REVIEWER
     */
    private String role;
    private String roleLabel;
    private String companyName;
    private LocalDateTime lastLoginAt;
}
