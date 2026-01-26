package com.smartchain.platform.dto.management.kpi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiFormulaItemDto {
    private Long formulaId;
    private String indicatorCode;
    private String indicatorName;
    private String formula;              // "Scope1 + Scope2"
    private String unit;                 // "tCO2eq"
    private String description;
    private String sourceStandard;       // GRI, IFRS
    private boolean isActive;
}
