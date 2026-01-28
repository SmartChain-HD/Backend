package com.smartchain.platform.dto.approval.list;

import com.smartchain.platform.dto.approval.common.DiagnosticSimpleDto;
import com.smartchain.platform.dto.approval.common.RequesterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListItemDto {
    private Long approvalId;             // v3.0: auditId → approvalId
    private DiagnosticSimpleDto diagnostic;  // v3.0: 진단 정보 구조화
    private RequesterDto requester;      // v3.0: 요청자 정보 구조화
    private String domainCode;           // 도메인 코드 (ESG, SAFETY, COMPLIANCE)
    private String domainName;           // 도메인 이름
    private String status;               // WAITING, APPROVED, REJECTED
    private String statusLabel;          // "결재 완료", "결재전", "반려"
    private LocalDateTime requestedAt;
    private String requestedAtLabel;     // "2026/01/09"
    private LocalDate deadline;          // v3.0: 마감일 추가
}
