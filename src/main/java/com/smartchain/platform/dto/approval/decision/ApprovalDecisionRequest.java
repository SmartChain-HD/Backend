package com.smartchain.platform.dto.approval.decision;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecisionRequest {

    @NotBlank(message = "결재 결과를 선택해주세요")
    private String decision;             // APPROVED, REJECTED

    private String comment;              // 코멘트 (반려시 권장)
}
