package com.smartchain.platform.dto.management.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementRequest {
    private String role;                 // 역할 필터 (GUEST, DRAFTER, APPROVER, REVIEWER)
    private String status;               // 상태 필터 (ACTIVE, INACTIVE)
    private Long companyId;              // 조직 필터
    private String keyword;              // v3.0: searchKeyword → keyword (API 명세 통일)
    private Integer page;
    private Integer size;
}
