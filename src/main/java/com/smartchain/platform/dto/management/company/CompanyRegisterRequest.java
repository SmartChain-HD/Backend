package com.smartchain.platform.dto.management.company;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterRequest {
    
    @NotBlank(message = "회사명을 입력해주세요")
    private String companyName;
    
    @NotBlank(message = "사업자등록번호를 입력해주세요")
    private String businessNumber;
    
    @NotBlank(message = "협력사 유형을 선택해주세요")
    private String companyType;          // TIER1, TIER2
    
    private String industryCode;
    private String ceoName;
    private String address;
    private String contactEmail;
    private String contactPhone;
    
    // 매핑 정보
    private Long assignedReviewerId;     // 담당 리뷰어
    private List<Long> diagnosticTemplateIds;   // 적용할 진단표 템플릿
}
