package com.smartchain.platform.dto.role.approval;

import com.smartchain.platform.dto.role.common.CompanySimpleDto;
import com.smartchain.platform.dto.role.common.RoleSimpleDto;
import com.smartchain.platform.dto.role.common.UserSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleApprovalDetailResponse {
    private Long accessRequestId;
    private UserSimpleDto user;
    private RoleSimpleDto requestedRole;
    private CompanySimpleDto company;
    private String status;
    private String reason;               // 신청 사유
    private LocalDateTime requestedAt;
}
