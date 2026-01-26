package com.smartchain.platform.dto.diagnostic.report;

import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitStatusDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReportPreviewResponse {
    private Long diagnosticId;
    private String reportTitle;          // "202X년도 (주)가나다 ESG 진단 보고서"
    private String companyName;
    private String reportPdfUrl;         // PDF 미리보기 URL
    private int totalPages;              // Page 1 / 12
    private int currentPage;
    private List<ReportTocItemDto> tableOfContents;      // 목차
    private DiagnosticSubmitStatusDto submitStatus;
}
