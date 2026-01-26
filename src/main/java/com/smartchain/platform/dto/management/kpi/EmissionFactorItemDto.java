package com.smartchain.platform.dto.management.kpi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmissionFactorItemDto {
    private Long factorId;
    private String fuelType;             // "LNG", "경유"
    private String unit;                 // "Nm3", "L"
    private double factor;               // 배출계수 값
    private String sourceYear;           // "2024"
    private String sourceOrg;            // "환경부"
    private boolean isActive;
}
