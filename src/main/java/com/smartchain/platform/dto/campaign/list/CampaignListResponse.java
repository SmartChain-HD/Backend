package com.smartchain.platform.dto.campaign.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignListResponse {
    private List<CampaignItemDto> campaigns;
    private int totalCount;
}
