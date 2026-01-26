package com.smartchain.platform.dto.review.decision;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskLevelUpdateRequest {
    
    @NotBlank(message = "변경할 위험군을 선택해주세요")
    private String newRiskLevel;         // HIGH, MEDIUM, LOW
    
    @NotBlank(message = "변경 사유를 입력해주세요")
    private String reason;
}
