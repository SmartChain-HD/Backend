package com.smartchain.platform.dto.auth.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoDto {
    private Long companyId;
    private String companyName;
    private String companyType;          // TIER1(1차협력사), TIER2(2차협력사)
}
