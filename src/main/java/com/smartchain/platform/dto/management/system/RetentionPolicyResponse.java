package com.smartchain.platform.dto.management.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyResponse {
    private int diagnosticRetentionDays;     // 진단 데이터 보관 기간
    private int fileRetentionDays;           // 파일 보관 기간
    private int logRetentionDays;            // 로그 보관 기간
    private boolean autoDeleteEnabled;       // 자동 삭제 활성화
    private LocalDateTime lastCleanupAt;     // 마지막 정리 일시
}
