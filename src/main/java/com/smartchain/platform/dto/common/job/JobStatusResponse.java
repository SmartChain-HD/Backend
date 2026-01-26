package com.smartchain.platform.dto.common.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {
    private String jobId;
    private String jobType;              // FILE_PARSING, REPORT_GENERATION, EXPORT
    private String status;               // PENDING, RUNNING, SUCCEEDED, FAILED
    private int progress;                // 0-100
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime estimatedCompletionAt;
    private JobResultDto result;         // 성공시
    private JobErrorDto error;           // 실패시
}
