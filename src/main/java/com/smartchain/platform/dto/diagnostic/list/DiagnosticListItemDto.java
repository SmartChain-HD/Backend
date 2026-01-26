package com.smartchain.platform.dto.diagnostic.list;

import com.smartchain.platform.dto.diagnostic.common.CampaignSimpleDto;
import com.smartchain.platform.dto.diagnostic.common.PeriodDto;
import com.smartchain.platform.dto.diagnostic.common.ProgressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticListItemDto {
    private Long diagnosticId;
    private String diagnosticCode;       // v3.0: "DG-2026-00001"
    private CampaignSimpleDto campaign;  // v3.0: 구조화
    private String summary;              // "○ 25년도 상반기 ESG 보고서 제..."
    private PeriodDto period;            // v3.0: 구조화
    private LocalDate deadline;          // 마감일
    private String status;               // WRITING, SUBMITTED, APPROVED, REJECTED
    private String statusLabel;          // "결재 완료", "결재전", "반려"
    private ProgressDto progress;        // v3.0: 구조화
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
