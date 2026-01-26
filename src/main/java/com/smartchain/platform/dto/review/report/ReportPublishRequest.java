package com.smartchain.platform.dto.review.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPublishRequest {
    private String reportType;           // FULL, SUMMARY
    private boolean includeAiAnalysis;   // v3.0: includeEvidence → includeAiAnalysis
    private boolean includeRecommendations; // v3.0: 추가
}
