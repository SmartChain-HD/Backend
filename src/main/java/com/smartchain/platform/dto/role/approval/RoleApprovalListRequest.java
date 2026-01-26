package com.smartchain.platform.dto.role.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleApprovalListRequest {
    private String status;               // PENDING, APPROVED, REJECTED
    private Long companyId;              // v3.0: REVIEWER만 사용 가능 (전체 협력사 조회)
    private Integer page;
    private Integer size;
}
