package com.smartchain.platform.dto.approval.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitToReviewerResponse {
    private Long approvalId;
    private Long diagnosticId;
    private Long reviewId;               // v3.0: 생성된 심사 ID
    private String previousStatus;
    private String newStatus;            // REVIEWING
    private LocalDateTime submittedAt;
    private String message;
}
