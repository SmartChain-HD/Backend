package com.smartchain.platform.dto.campaign.detail;

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
public class CampaignDetailResponse {
    private Long campaignId;
    private String campaignCode;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private String status;
    private String statusLabel;
    
    // 통계
    private CampaignStatsDto stats;
    
    // 대상 협력사 목록
    private List<CampaignTargetCompanyDto> targetCompanies;
    
    // 적용 진단표 템플릿
    private List<CampaignTemplateDto> templates;
}
