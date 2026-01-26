package com.smartchain.platform.dto.campaign.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreateResponse {
    private Long campaignId;
    private String campaignCode;
    private String title;
    private String message;
}
