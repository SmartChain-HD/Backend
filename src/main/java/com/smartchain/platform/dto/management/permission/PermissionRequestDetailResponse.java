package com.smartchain.platform.dto.management.permission;

import com.smartchain.platform.dto.management.common.CompanySimpleDto;
import com.smartchain.platform.dto.management.common.RoleSimpleDto;
import com.smartchain.platform.dto.management.common.UserSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDetailResponse {
    private Long accessRequestId;
    private UserSimpleDto user;
    private CompanySimpleDto company;
    private RoleSimpleDto requestedRole;
    private String status;
    private String reason;
    private LocalDateTime requestedAt;
    
    // 이전 권한 변경 이력
    private List<RoleHistoryItemDto> roleHistory;
}
