package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDashboardResponse {
    private OverviewDto overview;
    private RiskDistributionDto riskDistribution;
    private CategoryAveragesDto categoryAverages;
    private List<RecentActivityDto> recentActivities;
}
