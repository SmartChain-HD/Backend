package com.smartchain.platform.domain.job.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.enums.JobType;
import com.smartchain.platform.global.enums.PipelinePhase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "async_job")
public class AsyncJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    private PipelinePhase currentPhase;

    private int progress;

    @Column(length = 500)
    private String message;

    private Long requesterId;

    private Long targetId;  // reviewId, diagnosticId 등

    @Column(length = 2000)
    private String requestPayload;  // JSON 형태로 요청 정보 저장

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime estimatedCompletionAt;

    @Column(length = 1000)
    private String resultUrl;

    @Column(length = 500)
    private String errorCode;

    @Column(length = 1000)
    private String errorMessage;

    private boolean retryable;

    @Builder
    public AsyncJob(JobType jobType, Long requesterId, Long targetId, String requestPayload, Integer estimatedSeconds) {
        this.jobId = generateJobId(jobType);
        this.jobType = jobType;
        this.status = JobStatus.PENDING;
        this.currentPhase = PipelinePhase.QUEUED;
        this.progress = 0;
        this.requesterId = requesterId;
        this.targetId = targetId;
        this.requestPayload = requestPayload;
        this.message = "작업이 대기 중입니다";
        if (estimatedSeconds != null) {
            this.estimatedCompletionAt = LocalDateTime.now().plusSeconds(estimatedSeconds);
        }
    }

    private String generateJobId(JobType jobType) {
        String prefix = switch (jobType) {
            case FILE_PARSING -> "job_parse_";
            case REPORT_GENERATION -> "job_report_";
            case BULK_REPORT -> "job_bulk_report_";
            case EXPORT -> "job_export_";
            case AI_ESG_ANALYSIS -> "job_ai_esg_";
            case AI_COMPLIANCE_ANALYSIS -> "job_ai_compliance_";
            case AI_SAFETY_ANALYSIS -> "job_ai_safety_";
        };
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    public void start() {
        this.status = JobStatus.RUNNING;
        this.currentPhase = PipelinePhase.OCR;
        this.startedAt = LocalDateTime.now();
        this.message = "작업이 실행 중입니다";
    }

    public void updateProgress(int progress, String message) {
        this.progress = progress;
        this.message = message;
    }

    /**
     * 파이프라인 단계 진행 및 진행률 업데이트
     */
    public void advancePhase(PipelinePhase phase, int progress, String message) {
        this.currentPhase = phase;
        this.progress = progress;
        this.message = message;
    }

    /**
     * 다음 파이프라인 단계로 자동 진행
     */
    public void advanceToNextPhase() {
        if (this.currentPhase != null && !this.currentPhase.isTerminal()) {
            this.currentPhase = this.currentPhase.next();
            this.message = this.currentPhase.getDescription();
            // 단계별 진행률 계산 (4단계 기준)
            this.progress = Math.min(100, this.currentPhase.getOrder() * 25);
        }
    }

    public void succeed(String resultUrl) {
        this.status = JobStatus.SUCCEEDED;
        this.currentPhase = PipelinePhase.COMPLETED;
        this.progress = 100;
        this.completedAt = LocalDateTime.now();
        this.resultUrl = resultUrl;
        this.message = "작업이 완료되었습니다";
    }

    public void fail(String errorCode, String errorMessage, boolean retryable) {
        this.status = JobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
        this.message = "작업이 실패했습니다";
    }

    public String getStatusCheckUrl() {
        return "/api/v1/jobs/" + this.jobId;
    }

    public boolean isPending() {
        return this.status == JobStatus.PENDING;
    }

    public boolean isRunning() {
        return this.status == JobStatus.RUNNING;
    }

    public boolean isCompleted() {
        return this.status == JobStatus.SUCCEEDED || this.status == JobStatus.FAILED;
    }
}
