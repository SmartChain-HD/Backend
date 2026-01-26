package com.smartchain.platform.dto.management.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDecisionRequest {
    
    @NotBlank(message = "처리 결과를 선택해주세요")
    private String decision;             // APPROVED, REJECTED
    
    private String comment;              // 코멘트 (v3.0: rejectReason → comment 통일)
}
