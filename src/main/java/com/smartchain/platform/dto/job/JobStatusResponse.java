package com.smartchain.platform.dto.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobStatusResponse {
    private String jobId;
    private String jobType;
    private String status;
    private int progress;
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime estimatedCompletionAt;
    private JobResultDto result;
    private JobErrorDto error;

    @Getter
    @Builder
    public static class JobResultDto {
        private Long fileId;
        private Integer extractedIndicators;
        private String resultUrl;
    }

    @Getter
    @Builder
    public static class JobErrorDto {
        private String code;
        private String message;
        private boolean retryable;
    }
}
