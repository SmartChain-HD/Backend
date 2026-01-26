package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatusCardDto {
    private String status;               // 결재 완료 / 결재전
    private String statusCode;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedAtLabel;   // "2026-01-09"
}
