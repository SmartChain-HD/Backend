package com.smartchain.platform.dto.diagnostic.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticSubmitResponse {
    private Long diagnosticId;
    private String previousStatus;
    private String newStatus;            // SUBMITTED
    private LocalDateTime submittedAt;
    private Long approvalId;             // v3.0: 생성된 결재 ID
    private String message;
}
