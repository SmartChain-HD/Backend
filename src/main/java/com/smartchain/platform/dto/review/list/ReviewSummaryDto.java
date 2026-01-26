package com.smartchain.platform.dto.review.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private int totalCompanies;          // 전체 협력사 수
    private int completedCount;          // 완료
    private int inProgressCount;         // 검수중
    private int pendingCount;            // 대기
    private int highRiskCount;           // 고위험군 수
    private int mediumRiskCount;         // 중위험군 수
    private int lowRiskCount;            // 저위험군 수
}
