package com.smartchain.platform.dto.auth.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleInfoDto {
    /**
     * 역할 코드
     * 
     * ⚠️ v3.0 변경: ADMIN 역할 제거
     * 
     * 가능한 값:
     * - GUEST: 게스트 (승인 대기)
     * - DRAFTER: 기안자 (협력사 작성 직원)
     * - APPROVER: 결재자 (협력사 팀장)
     * - REVIEWER: 수신자 (원청 ESG 관리팀) - 권한 관리 기능 포함
     */
    private String code;
    
    /**
     * 역할명
     * - 게스트, 기안자, 결재자, 수신자
     */
    private String name;
    
    private List<String> permissions;
}
