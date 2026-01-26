package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportTabResponse {
    private String reportPdfUrl;
    private String downloadUrl;
    private String riskLevel;            // HIGH, MEDIUM, LOW
    private String riskLevelLabel;       // "고위험군"
    private String riskColorClass;       // CSS 클래스
    private LocalDateTime generatedAt;
    private String generatedAtLabel;
}
