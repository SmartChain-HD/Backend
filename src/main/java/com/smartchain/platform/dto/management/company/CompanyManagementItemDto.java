package com.smartchain.platform.dto.management.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyManagementItemDto {
    private Long companyId;
    private String companyName;
    private String businessNumber;
    private String companyType;
    private IndustryInfoDto industry;    // v3.0: 업종 정보 구조화
    private int userCount;               // 소속 사용자 수
    private int diagnosticCount;         // 진단 건수
    private String status;               // ACTIVE, INACTIVE
    private LocalDateTime registeredAt;
}
