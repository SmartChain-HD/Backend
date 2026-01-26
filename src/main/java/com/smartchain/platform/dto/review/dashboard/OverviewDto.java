package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewDto {
    private int totalCompanies;
    private int submittedCount;
    private int inReviewCount;
    private int completedCount;
    private int notStartedCount;
}
