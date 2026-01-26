package com.smartchain.platform.dto.campaign.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTargetCompanyDto {
    private Long companyId;
    private String companyName;
    private String companyType;
    private String assignedReviewer;
    private String currentStatus;
    private String currentStatusLabel;
    private LocalDateTime lastUpdatedAt;
}
