package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.ai.client.AiRunApiClient;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.dto.ai.run.FileInfo;
import com.smartchain.platform.dto.ai.run.RunPreviewRequest;
import com.smartchain.platform.dto.ai.run.RunPreviewResponse;
import com.smartchain.platform.dto.ai.run.SlotHint;
import com.smartchain.platform.global.enums.PipelinePhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileParsingJobService {

    private final AsyncJobRepository asyncJobRepository;
    private final EvidenceFileRepository evidenceFileRepository;
    private final AiRunApiClient aiRunApiClient;

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

            // Phase 1: OCR
            job.advancePhase(PipelinePhase.OCR, 25, "파일 OCR 처리 중");
            asyncJobRepository.save(job);

            // AI preview API 호출 (파일 파싱)
            Diagnostic diagnostic = file.getDiagnostic();
            Long diagnosticId = diagnostic != null ? diagnostic.getDiagnosticId() : null;

            String domainCode = "esg";
            String periodStart = null;
            String periodEnd = null;
            if (diagnostic != null) {
                if (diagnostic.getDomain() != null) {
                    domainCode = diagnostic.getDomain().getCode().toLowerCase();
                }
                if (diagnostic.getPeriodStartDate() != null) {
                    periodStart = diagnostic.getPeriodStartDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                if (diagnostic.getPeriodEndDate() != null) {
                    periodEnd = diagnostic.getPeriodEndDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            }

            RunPreviewRequest previewRequest = new RunPreviewRequest(
                    domainCode,
                    periodStart,
                    periodEnd,
                    null,  // packageId - 첫 호출이므로 null
                    List.of(new FileInfo(
                            file.getResultFileId().toString(),
                            file.getFilePath(),
                            file.getOriginalFileName()
                    ))
            );

            RunPreviewResponse previewResponse = aiRunApiClient.previewSync(previewRequest);

            // Phase 2: VALIDATION
            job.advancePhase(PipelinePhase.VALIDATION, 50, "파싱 결과 검증 중");
            asyncJobRepository.save(job);

            // Phase 3: METRICS
            job.advancePhase(PipelinePhase.METRICS, 75, "데이터 추출 완료");
            asyncJobRepository.save(job);

            // 파싱 결과를 EvidenceFile에 반영
            String parsedText = extractSlotInfo(previewResponse);
            String metaInfo = buildMetaInfo(previewResponse);

            file.completeParsing(null, parsedText, metaInfo);
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

    private String extractSlotInfo(RunPreviewResponse response) {
        if (response == null || response.slotHint() == null) return null;
        return response.slotHint().stream()
                .map(SlotHint::slotName)
                .collect(Collectors.joining(", "));
    }

    private String buildMetaInfo(RunPreviewResponse response) {
        if (response == null) return null;
        int slotCount = response.slotHint() != null ? response.slotHint().size() : 0;
        int missingCount = response.missingRequiredSlots() != null ? response.missingRequiredSlots().size() : 0;
        return "{\"packageId\":\"" + response.packageId()
                + "\",\"slotHintCount\":" + slotCount
                + ",\"missingRequiredSlots\":" + missingCount
                + "}";
    }
}
