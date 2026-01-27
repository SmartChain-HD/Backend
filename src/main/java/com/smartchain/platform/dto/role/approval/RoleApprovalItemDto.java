package com.smartchain.platform.dto.role.approval;

import com.smartchain.platform.dto.role.common.CompanySimpleDto;
import com.smartchain.platform.dto.role.common.DomainSimpleDto;
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
public class RoleApprovalItemDto {
    private Long accessRequestId;        // v3.0: requestId → accessRequestId
    private UserSimpleDto user;          // v3.0: 구조화
    private DomainSimpleDto domain;      // v4.0: 도메인 정보
    private CompanySimpleDto company;    // v3.0: 회사 정보 추가
    private RoleSimpleDto requestedRole; // v3.0: 구조화
    private String status;               // PENDING, APPROVED, REJECTED
    private String reason;               // 요청 사유
    private LocalDateTime requestedAt;
    private String requestedAtLabel;     // "26.01.09"
}
