package com.smartchain.platform.dto.management.permission;

import com.smartchain.platform.dto.management.common.CompanySimpleDto;
import com.smartchain.platform.dto.management.common.RoleSimpleDto;
import com.smartchain.platform.dto.management.common.UserSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestItemDto {
    private Long accessRequestId;        // v3.0: requestId → accessRequestId (API 명세 통일)
    private UserSimpleDto user;          // v3.0: 사용자 정보 구조화
    private CompanySimpleDto company;    // v3.0: 회사 정보 구조화
    private RoleSimpleDto requestedRole; // v3.0: 역할 정보 구조화
    private String status;               // PENDING, APPROVED, REJECTED
    private LocalDateTime requestedAt;
    private String requestedAtLabel;     // "2026-01-09 15:40:30"
}
