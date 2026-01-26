package com.smartchain.platform.dto.approval.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatusDto {
    private String status;               // WAITING, APPROVED, REJECTED
    private String statusLabel;          // "결재 완료"
    private String statusColorClass;     // CSS 클래스 (green/yellow/red)
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedAtLabel;   // "2026-01-09"
}
