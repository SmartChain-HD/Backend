package com.smartchain.platform.dto.approval.decision;

import com.smartchain.platform.dto.approval.detail.ProcessedByDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecisionResponse {
    private Long approvalId;
    private String status;
    private String diagnosticNewStatus;  // v3.0: 진단 상태 변경 정보 추가
    private LocalDateTime processedAt;
    private ProcessedByDto processedBy;
    private String message;              // "결재가 완료되었습니다"
}
