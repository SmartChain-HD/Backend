package com.smartchain.platform.dto.management.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyManagementRequest {
    private String companyType;          // TIER1, TIER2
    private String industryCode;         // 업종 코드
    private String keyword;              // v3.0: searchKeyword → keyword
    private Integer page;
    private Integer size;
}
