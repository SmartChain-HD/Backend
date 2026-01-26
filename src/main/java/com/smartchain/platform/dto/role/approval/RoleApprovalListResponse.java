package com.smartchain.platform.dto.role.approval;

import com.smartchain.platform.dto.role.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleApprovalListResponse {
    private List<RoleApprovalItemDto> content;   // v3.0: 통일
    private PageDto page;                        // v3.0: 페이지네이션 표준화
}
