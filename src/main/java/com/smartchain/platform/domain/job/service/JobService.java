package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.dto.job.JobRetryResponse;
import com.smartchain.platform.dto.job.JobStatusResponse;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final AsyncJobRepository asyncJobRepository;

    public JobStatusResponse getJobStatus(Long userId, String jobId) {
        AsyncJob job = asyncJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_NOT_FOUND));

        // 본인이 요청한 작업인지 확인
        if (!job.getRequesterId().equals(userId)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        return buildJobStatusResponse(job);
    }

    @Transactional
    public JobRetryResponse retryJob(Long userId, String jobId) {
        AsyncJob originalJob = asyncJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_NOT_FOUND));

        // 본인이 요청한 작업인지 확인
        if (!originalJob.getRequesterId().equals(userId)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        // 실패한 작업인지 확인
        if (originalJob.getStatus() != JobStatus.FAILED) {
            throw new CustomException(ErrorCode.JOB_NOT_FAILED);
        }

        // 재시도 가능한지 확인
        if (!originalJob.isRetryable()) {
            throw new CustomException(ErrorCode.JOB_NOT_RETRYABLE);
        }

        // 새 작업 생성
        AsyncJob newJob = AsyncJob.builder()
                .jobType(originalJob.getJobType())
                .requesterId(originalJob.getRequesterId())
                .targetId(originalJob.getTargetId())
                .requestPayload(originalJob.getRequestPayload())
                .build();

        asyncJobRepository.save(newJob);

        log.info("Job retry requested: originalJobId={}, newJobId={}, userId={}",
                jobId, newJob.getJobId(), userId);

        return JobRetryResponse.builder()
                .newJobId(newJob.getJobId())
                .status(newJob.getStatus().name())
                .statusCheckUrl(newJob.getStatusCheckUrl())
                .build();
    }

    private JobStatusResponse buildJobStatusResponse(AsyncJob job) {
        JobStatusResponse.JobStatusResponseBuilder builder = JobStatusResponse.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType().name())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .message(job.getMessage())
                .startedAt(job.getStartedAt())
                .estimatedCompletionAt(job.getEstimatedCompletionAt());

        if (job.getStatus() == JobStatus.SUCCEEDED) {
            builder.completedAt(job.getCompletedAt())
                    .result(JobStatusResponse.JobResultDto.builder()
                            .resultUrl(job.getResultUrl())
                            .build());
        } else if (job.getStatus() == JobStatus.FAILED) {
            builder.completedAt(job.getCompletedAt())
                    .error(JobStatusResponse.JobErrorDto.builder()
                            .code(job.getErrorCode())
                            .message(job.getErrorMessage())
                            .retryable(job.isRetryable())
                            .build());
        }

        return builder.build();
    }
}
