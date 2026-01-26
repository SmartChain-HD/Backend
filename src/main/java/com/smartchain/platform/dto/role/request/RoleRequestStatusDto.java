package com.smartchain.platform.dto.role.request;

import com.smartchain.platform.dto.role.common.CompanySimpleDto;
import com.smartchain.platform.dto.role.common.ProcessedByDto;
import com.smartchain.platform.dto.role.common.RoleSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestStatusDto {
    private Long accessRequestId;        // v3.0: requestId → accessRequestId
    private RoleSimpleDto requestedRole; // v3.0: 구조화
    private CompanySimpleDto company;    // v3.0: 구조화
    private String status;               // PENDING, APPROVED, REJECTED
    private String statusLabel;          // "승인 대기중", "승인 완료", "승인 반려"
    private String reason;               // 요청 사유
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private ProcessedByDto processedBy;  // v3.0: 구조화
    private String rejectReason;         // 반려 사유 (반려시)
}
