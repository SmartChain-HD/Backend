package com.smartchain.platform.dto.review.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPublishResponse {
    private String jobId;                // v3.0: 비동기 처리
    private String status;               // PENDING
    private String statusCheckUrl;
    private Integer estimatedCompletionTime; // 예상 완료 시간 (초)
    private String message;
}
