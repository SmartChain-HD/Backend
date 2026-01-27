package com.smartchain.platform.dto.diagnostic.detail;

import com.smartchain.platform.dto.diagnostic.common.CompanySimpleDto;
import com.smartchain.platform.dto.diagnostic.common.DomainSimpleDto;
import com.smartchain.platform.dto.diagnostic.common.PeriodDto;
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
public class DiagnosticDetailResponse {
    private Long diagnosticId;
    private String diagnosticCode;
    private DomainSimpleDto domain;          // v3.0: 도메인 정보
    private CampaignDetailDto campaign;      // v3.0: 구조화
    private CompanySimpleDto company;        // v3.0: 구조화
    private PeriodDto period;
    private LocalDate deadline;
    private String status;
    private String statusLabel;
    private int qualitativeProgress;
    private int quantitativeProgress;
    private int overallProgress;
    private CreatedByDto createdBy;          // v3.0: 구조화
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
}
