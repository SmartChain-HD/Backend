package com.smartchain.platform.dto.campaign.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTemplateDto {
    private Long templateId;
    private String templateName;
    private String templateCode;
    private int questionCount;
    private int indicatorCount;
}
