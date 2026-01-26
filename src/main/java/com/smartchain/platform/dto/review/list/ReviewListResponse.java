package com.smartchain.platform.dto.review.list;

import com.smartchain.platform.dto.review.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponse {
    private ReviewSummaryDto summary;    // v3.0: 순서 변경 (요약 먼저)
    private List<ReviewListItemDto> content;  // v3.0: items → content
    private PageDto page;                // v3.0: 페이지네이션 표준화
}
