package com.smartchain.platform.dto.management.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySimpleDto {
    private Long companyId;
    private String companyName;
    private String companyType;          // TIER1, TIER2
}
