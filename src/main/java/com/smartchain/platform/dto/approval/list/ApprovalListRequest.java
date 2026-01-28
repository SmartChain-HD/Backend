package com.smartchain.platform.dto.approval.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListRequest {
    private String domainCode;           // 도메인 필터 (ESG, SAFETY, COMPLIANCE)
    private String status;               // WAITING, APPROVED, REJECTED
    private LocalDate fromDate;          // 기간 필터 시작
    private LocalDate toDate;            // 기간 필터 종료
    private Integer page;
    private Integer size;
}
