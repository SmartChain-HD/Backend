package com.smartchain.platform.dto.role.page;

import com.smartchain.platform.dto.role.common.RoleSimpleDto;
import com.smartchain.platform.dto.role.request.RoleRequestStatusDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestPageResponse {
    private RoleSimpleDto currentRole;           // v3.0: 구조화
    private List<RoleOptionDto> availableRoles;
    private List<DomainOptionDto> availableDomains;  // v4.0: 도메인 목록
    private List<CompanyOptionDto> availableCompanies;
    private RoleRequestStatusDto pendingRequest; // 대기중인 요청 (있을 경우)
}
