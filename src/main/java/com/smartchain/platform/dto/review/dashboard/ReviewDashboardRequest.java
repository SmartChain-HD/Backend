package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDashboardRequest {
    private String domainCode;           // 도메인 필터 (ESG, SAFETY, COMPLIANCE)
    private Long campaignId;             // 캠페인 필터
    private LocalDate fromDate;          // 기간 시작
    private LocalDate toDate;            // 기간 종료
}
