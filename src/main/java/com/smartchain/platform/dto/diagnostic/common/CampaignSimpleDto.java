package com.smartchain.platform.dto.diagnostic.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSimpleDto {
    private Long campaignId;
    private String campaignCode;         // "CMP-2026-001"
    private String title;                // "2025년도 상반기 ESG 자가진단"
    private String status;               // "ACTIVE", "COMPLETED" - 기안 상태 기반
    private String statusLabel;          // "진행중", "완료"
}
