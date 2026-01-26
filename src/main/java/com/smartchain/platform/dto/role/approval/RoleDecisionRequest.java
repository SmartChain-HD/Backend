package com.smartchain.platform.dto.role.approval;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDecisionRequest {
    
    @NotBlank(message = "처리 결과를 선택해주세요")
    private String decision;             // APPROVED, REJECTED
    
    private String rejectReason;         // 반려시 필수
}
