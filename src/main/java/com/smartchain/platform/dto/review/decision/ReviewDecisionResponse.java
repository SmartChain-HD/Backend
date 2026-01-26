package com.smartchain.platform.dto.review.decision;

import com.smartchain.platform.dto.review.common.ProcessedByDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDecisionResponse {
    private Long reviewId;               // v3.0: auditId → reviewId
    private String status;
    private String statusLabel;
    private LocalDateTime processedAt;
    private ProcessedByDto processedBy;  // v3.0: 처리자 정보 추가
    private String message;
    private String nextStep;             // 다음 단계 안내
}
