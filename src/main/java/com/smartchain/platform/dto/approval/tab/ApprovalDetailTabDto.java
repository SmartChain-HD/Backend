package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDetailTabDto {
    private String tabCode;              // DIAGNOSTIC_PDF, DATA_PACKAGE, MODIFICATION_LOG, AI_REPORT
    private String tabName;              // "진단표 PDF", "데이터 패키지", "수정 로그", "AI 진단 보고서"
    private boolean isActive;            // 현재 선택된 탭
}
