package com.smartchain.platform.dto.review.decision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskLevelUpdateResponse {
    private Long reviewId;               // v3.0: auditId → reviewId
    private String previousLevel;
    private String previousLevelLabel;
    private String newLevel;
    private String newLevelLabel;
    private LocalDateTime changedAt;
    private String changedBy;
    private String message;
}
