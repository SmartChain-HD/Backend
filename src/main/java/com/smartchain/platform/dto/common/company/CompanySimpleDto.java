package com.smartchain.platform.dto.common.company;

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
    private String companyType;      // TIER1(1차협력사), TIER2(2차협력사)
    private String industryCode;     // 업종 코드
    private String industryName;     // 업종명
}
