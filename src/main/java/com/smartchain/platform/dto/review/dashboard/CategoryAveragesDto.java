package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAveragesDto {
    private double E;                    // 환경 평균
    private double S;                    // 사회 평균
    private double G;                    // 지배구조 평균
}
