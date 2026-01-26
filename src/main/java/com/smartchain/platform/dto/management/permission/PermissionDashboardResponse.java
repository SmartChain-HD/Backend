package com.smartchain.platform.dto.management.permission;

import com.smartchain.platform.dto.management.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDashboardResponse {
    private PermissionStatsDto stats;
    private List<PermissionRequestItemDto> content;  // v3.0: requests → content (API 명세 통일)
    private PageDto page;                            // v3.0: 페이지네이션 표준화
}
