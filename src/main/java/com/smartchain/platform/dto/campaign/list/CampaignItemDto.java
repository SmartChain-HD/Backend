package com.smartchain.platform.dto.campaign.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignItemDto {
    private Long campaignId;
    private String campaignCode;         // "CMP-2026-001"
    private String title;                // "2026년 상반기 ESG 평가"
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private String status;               // DRAFT, ACTIVE, CLOSED
    private String statusLabel;
    private int targetCompanyCount;      // 대상 협력사 수
    private int submittedCount;          // 제출 완료 수
    private int progressRate;            // 진행률 (%)
}
