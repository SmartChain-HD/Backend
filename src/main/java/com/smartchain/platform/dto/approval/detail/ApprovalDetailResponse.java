package com.smartchain.platform.dto.approval.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDetailResponse {
    private Long approvalId;
    private DiagnosticDetailDto diagnostic;  // v3.0: 구조화
    private RequesterDetailDto requester;    // v3.0: 구조화
    private String domainCode;           // 도메인 코드 (ESG, SAFETY, COMPLIANCE)
    private String domainName;           // 도메인 이름
    private String status;
    private String statusLabel;
    private String requestComment;       // v3.0: 요청 코멘트 추가
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private ProcessedByDto processedBy;
    private String approverComment;      // v3.0: 결재자 코멘트 추가
}
