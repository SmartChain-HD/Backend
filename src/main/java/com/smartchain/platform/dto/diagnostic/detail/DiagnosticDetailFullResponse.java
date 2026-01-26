package com.smartchain.platform.dto.diagnostic.detail;

import com.smartchain.platform.dto.diagnostic.report.DataPackageInfoDto;
import com.smartchain.platform.dto.diagnostic.report.ModificationLogDto;
import com.smartchain.platform.dto.diagnostic.report.AiReportInfoDto;
import com.smartchain.platform.dto.diagnostic.report.ApprovalStatusCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticDetailFullResponse {
    private Long diagnosticId;
    private String campaignId;
    private String campaignTitle;
    private String companyName;
    private String period;
    private LocalDate deadline;
    private String status;
    private String statusLabel;
    
    // 탭별 데이터
    private String diagnosticPdfUrl;
    private DataPackageInfoDto dataPackage;
    private List<ModificationLogDto> modificationLogs;
    private AiReportInfoDto aiReport;
    
    // 상태 카드
    private ApprovalStatusCardDto approvalStatus;
    
    // 가능한 액션
    private List<String> availableActions;   // ["SUBMIT", "EDIT", "DELETE"]
}
