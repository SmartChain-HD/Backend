package com.smartchain.platform.dto.role.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleOptionDto {
    /**
     * 역할 코드
     * 
     * ⚠️ v3.0 변경: ADMIN 역할 제거
     * 
     * 가능한 값:
     * - DRAFTER: 기안자
     * - APPROVER: 결재자
     * - REVIEWER: 수신자 (권한 관리 기능 포함)
     */
    private String roleCode;
    private String roleName;             // 기안자, 결재자, 수신자
    private String description;          // "ESG 자가 진단 데이터 업로드"
    private String iconUrl;              // 아이콘 URL
    private boolean selectable;          // 선택 가능 여부
}
