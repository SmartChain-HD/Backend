package com.smartchain.platform.dto.common.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDto {
    private Long companyId;
    private String companyName;
    private String businessNumber;       // 사업자등록번호
    private String companyType;
    private String industryCode;
    private String industryName;
    private String ceoName;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private Integer employeeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
