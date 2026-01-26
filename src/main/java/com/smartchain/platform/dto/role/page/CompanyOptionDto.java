package com.smartchain.platform.dto.role.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyOptionDto {
    private Long companyId;
    private String companyName;          // "(주)00제조"
    private String companyType;          // "TIER1" (1차협력사)
}
