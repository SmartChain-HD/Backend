package com.smartchain.platform.dto.diagnostic.quantitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantIndicatorItemDto {
    private Long indicatorId;
    private String indicatorCode;        // "GHG-001"
    private String indicatorName;        // "Scope 1 온실가스 배출량"
    private String unit;                 // "tCO2eq"
    private String dataType;             // NUMBER
    private boolean required;
    private CurrentValueDto currentValue;
    private List<HistoricalValueDto> historicalValues;
}
