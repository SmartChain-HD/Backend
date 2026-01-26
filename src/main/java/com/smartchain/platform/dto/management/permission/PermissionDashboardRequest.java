package com.smartchain.platform.dto.management.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDashboardRequest {
    private LocalDate date;              // 기준 날짜
    private Long companyId;              // 회사/협력사 필터
    private String companyType;          // TIER1, TIER2
    private String requestedRole;        // DRAFTER, APPROVER, REVIEWER
    private String status;               // PENDING, APPROVED, REJECTED
    private Integer page;
    private Integer size;
}
