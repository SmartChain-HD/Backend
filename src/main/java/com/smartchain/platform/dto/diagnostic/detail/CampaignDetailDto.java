package com.smartchain.platform.dto.diagnostic.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDetailDto {
    private Long campaignId;
    private String campaignCode;
    private String title;
    private List<String> disclosureStandards;  // ["GRI", "IFRS_S1_S2"]
}
