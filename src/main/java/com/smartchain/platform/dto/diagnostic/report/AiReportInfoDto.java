package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportInfoDto {
    private String reportPdfUrl;
    private String riskLevel;            // HIGH, MEDIUM, LOW
    private String riskLevelLabel;       // "고위험군"
    private String executiveSummary;     // 요약
    private LocalDateTime generatedAt;
}
