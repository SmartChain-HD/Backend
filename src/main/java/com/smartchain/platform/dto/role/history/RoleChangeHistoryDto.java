package com.smartchain.platform.dto.role.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeHistoryDto {
    private Long historyId;
    private String userName;
    private String previousRole;
    private String newRole;
    private String changedBy;            // 변경 처리자
    /**
     * 변경 유형
     * 
     * ⚠️ v3.0 변경: ADMIN_CHANGE → REVIEWER_CHANGE
     * 
     * 가능한 값:
     * - APPROVAL: 권한 요청 승인
     * - REVOKE: 권한 회수
     * - REVIEWER_CHANGE: 수신자에 의한 직접 변경
     */
    private String changeType;
    private LocalDateTime changedAt;
    private String reason;
}
