package com.smartchain.platform.dto.approval.list;

import com.smartchain.platform.dto.approval.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListResponse {
    private ApprovalStatsDto stats;      // v3.0: 통계 추가
    private List<ApprovalListItemDto> content;  // v3.0: items → content
    private PageDto page;                // v3.0: 페이지네이션 표준화
}
