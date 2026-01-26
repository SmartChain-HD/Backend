package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDistributionDto {
    private int high;
    private int medium;
    private int low;
}
