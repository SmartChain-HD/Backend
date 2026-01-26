package com.smartchain.platform.dto.role.approval;

import com.smartchain.platform.dto.role.common.ProcessedByDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDecisionResponse {
    private Long accessRequestId;        // v3.0: requestId → accessRequestId
    private String status;               // APPROVED, REJECTED
    private LocalDateTime processedAt;
    private ProcessedByDto processedBy;  // v3.0: 처리자 정보 추가
    private String message;              // "권한 요청이 승인되었습니다"
}
