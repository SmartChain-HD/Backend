package com.smartchain.platform.dto.campaign.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    // 기안자용
    private Integer myDraftCount;
    private Integer myPendingCount;
    private Integer myApprovedCount;
    
    // 결재자용
    private Integer pendingApprovalCount;
    private Integer todayApprovedCount;
    
    /**
     * 수신자용 통계
     * 
     * ⚠️ v3.0 변경: 기존 관리자용 통계가 수신자용으로 통합됨
     * - REVIEWER가 권한 관리 기능을 담당하므로 pendingPermissionCount 포함
     */
    private Integer totalSupplierCount;
    private Integer reviewPendingCount;
    private Integer highRiskCount;
    private Integer pendingPermissionCount;  // v3.0: 수신자가 권한 관리 담당
    
    // v3.0 제거: 관리자용 필드 제거
    // private Integer totalUserCount;       // 제거됨 - 필요시 수신자용으로 추가
    // private Integer pendingPermissionCount; // 수신자용으로 이동
}
