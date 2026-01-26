package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTocIndicatorDto {
    private String indicatorCode;        // "Q1", "Q2"
    private String indicatorName;        // "온실가스 배출량"
    private int pageNumber;
}
