package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.global.enums.PipelinePhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileParsingJobService {

    private final AsyncJobRepository asyncJobRepository;
    private final EvidenceFileRepository evidenceFileRepository;

    @Async("fileParsingExecutor")
    @Transactional
    public CompletableFuture<Void> executeParsingAsync(String jobId) {
        AsyncJob job = asyncJobRepository.findByJobId(jobId).orElse(null);
        if (job == null) {
            log.error("File parsing job not found: jobId={}", jobId);
            return CompletableFuture.completedFuture(null);
        }

        EvidenceFile file = evidenceFileRepository.findById(job.getTargetId()).orElse(null);
        if (file == null) {
            log.error("EvidenceFile not found: fileId={}, jobId={}", job.getTargetId(), jobId);
            job.fail("FILE_NOT_FOUND", "파싱 대상 파일을 찾을 수 없습니다", false);
            asyncJobRepository.save(job);
            return CompletableFuture.completedFuture(null);
        }

        try {
            // PENDING → RUNNING
            job.start();
            asyncJobRepository.save(job);
            file.startParsing();
            evidenceFileRepository.save(file);
            log.info("File parsing started: jobId={}, fileId={}", jobId, file.getResultFileId());

            // Phase 1: 파일 검증
            job.advancePhase(PipelinePhase.VALIDATION, 50, "파일 검증 중");
            asyncJobRepository.save(job);

            Diagnostic diagnostic = file.getDiagnostic();
            Long diagnosticId = diagnostic != null ? diagnostic.getDiagnosticId() : null;

            // 파일 업로드 완료 처리 (AI preview는 프론트엔드에서 수동 호출)
            file.completeParsing(null, null, null);
            evidenceFileRepository.save(file);

            // 완료
            String resultUrl = "/api/v1/diagnostics/" + diagnosticId
                    + "/files/" + file.getResultFileId() + "/parsing-result";
            job.succeed(resultUrl);
            asyncJobRepository.save(job);

            log.info("File parsing completed: jobId={}, fileId={}", jobId, file.getResultFileId());

        } catch (Exception e) {
            log.error("File parsing failed: jobId={}, error={}", jobId, e.getMessage(), e);
            job.fail("PARSING_ERROR", e.getMessage(), true);
            asyncJobRepository.save(job);
            file.failParsing(e.getMessage());
            evidenceFileRepository.save(file);
        }

        return CompletableFuture.completedFuture(null);
    }

}
