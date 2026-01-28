package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.job.JobStatusResponse;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.enums.JobType;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisJobService {

    private final AsyncJobRepository asyncJobRepository;
    private final DiagnosticRepository diagnosticRepository;
    private final UserRepository userRepository;

    private static final int AI_ANALYSIS_ESTIMATED_SECONDS = 120;

    @Transactional
    public JobStatusResponse submitAiAnalysis(Long userId, Long diagnosticId, String analysisType) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 권한 검증: DRAFTER 또는 APPROVER
        validateAccess(currentUser, diagnostic);

        JobType jobType = resolveJobType(analysisType);

        AsyncJob job = AsyncJob.builder()
                .jobType(jobType)
                .requesterId(userId)
                .targetId(diagnosticId)
                .requestPayload("{\"diagnosticId\":" + diagnosticId + ",\"analysisType\":\"" + analysisType + "\"}")
                .estimatedSeconds(AI_ANALYSIS_ESTIMATED_SECONDS)
                .build();

        asyncJobRepository.save(job);

        log.info("AI analysis job created: jobId={}, diagnosticId={}, type={}, requesterId={}",
                job.getJobId(), diagnosticId, analysisType, userId);

        // 비동기 실행
        executeAnalysisAsync(job.getJobId());

        return JobStatusResponse.builder()
                .jobId(job.getJobId())
                .jobType(jobType.name())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .message("AI 분석이 시작되었습니다")
                .estimatedCompletionAt(job.getEstimatedCompletionAt())
                .build();
    }

    @Async("aiAnalysisExecutor")
    @Transactional
    public CompletableFuture<Void> executeAnalysisAsync(String jobId) {
        AsyncJob job = asyncJobRepository.findByJobId(jobId)
                .orElse(null);

        if (job == null) {
            log.error("AI analysis job not found: jobId={}", jobId);
            return CompletableFuture.completedFuture(null);
        }

        try {
            job.start();
            asyncJobRepository.save(job);
            log.info("AI analysis started: jobId={}", jobId);

            // 진행률 업데이트
            job.updateProgress(30, "데이터 수집 중");
            asyncJobRepository.save(job);

            job.updateProgress(60, "AI 분석 수행 중");
            asyncJobRepository.save(job);

            job.updateProgress(90, "결과 정리 중");
            asyncJobRepository.save(job);

            // 분석 완료
            String resultUrl = "/api/v1/diagnostics/" + job.getTargetId() + "/ai-analysis";
            job.succeed(resultUrl);
            asyncJobRepository.save(job);

            log.info("AI analysis completed: jobId={}, targetId={}", jobId, job.getTargetId());

        } catch (Exception e) {
            log.error("AI analysis failed: jobId={}, error={}", jobId, e.getMessage(), e);
            job.fail("AI_ERROR", e.getMessage(), true);
            asyncJobRepository.save(job);
        }

        return CompletableFuture.completedFuture(null);
    }

    private JobType resolveJobType(String analysisType) {
        if (analysisType == null) {
            return JobType.AI_ESG_ANALYSIS;
        }
        return switch (analysisType.toUpperCase()) {
            case "ESG" -> JobType.AI_ESG_ANALYSIS;
            case "COMPLIANCE" -> JobType.AI_COMPLIANCE_ANALYSIS;
            case "SAFETY" -> JobType.AI_SAFETY_ANALYSIS;
            default -> JobType.AI_ESG_ANALYSIS;
        };
    }

    private void validateAccess(User user, Diagnostic diagnostic) {
        var domain = diagnostic.getDomain();
        if (domain != null) {
            if (!user.hasAnyRoleInDomain(domain.getCode(), "DRAFTER", "APPROVER", "REVIEWER")) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }
            return;
        }
        // 레거시: 도메인 없는 경우 기본 역할 검증
        String roleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!"DRAFTER".equals(roleCode) && !"APPROVER".equals(roleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }
}
