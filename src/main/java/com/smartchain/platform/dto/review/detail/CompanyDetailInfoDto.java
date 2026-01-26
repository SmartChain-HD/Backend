package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailInfoDto {
    private Long companyId;
    private String companyName;
    private String industry;
    private String contactPerson;
    private String contactEmail;
}
