package com.smartchain.platform.dto.diagnostic.quantitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalValueDto {
    private int year;
    private BigDecimal value;
}
