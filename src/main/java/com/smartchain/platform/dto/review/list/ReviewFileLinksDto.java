package com.smartchain.platform.dto.review.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFileLinksDto {
    private String diagnosticPdfUrl;     // 진단표 PDF
    private String dataPackageUrl;       // 데이터 패키지 ZIP
    private String modificationLogUrl;   // 수정 로그
    private String aiReportUrl;          // AI 진단 보고서
    private boolean hasDiagnosticPdf;
    private boolean hasDataPackage;
    private boolean hasModificationLog;
    private boolean hasAiReport;
}
