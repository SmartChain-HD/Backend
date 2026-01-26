package com.smartchain.platform.dto.campaign.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreateRequest {
    
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    @NotNull
    private LocalDate deadline;
    
    private List<Long> targetCompanyIds;     // 대상 협력사 ID 목록
    private List<Long> templateIds;          // 적용 템플릿 ID 목록
}
