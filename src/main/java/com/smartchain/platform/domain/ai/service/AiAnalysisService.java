package com.smartchain.platform.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.ai.client.AiRunApiClient;
import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.domain.ai.repository.AiAnalysisResultRepository;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.dto.ai.run.*;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 분석 서비스
 * Diagnostic과 AI Run API를 연동
 */
@Service
@Transactional(readOnly = true)
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AiRunApiClient aiRunApiClient;
    private final AiAnalysisResultRepository resultRepository;
    private final DiagnosticRepository diagnosticRepository;
    private final EvidenceFileRepository evidenceFileRepository;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(
        AiRunApiClient aiRunApiClient,
        AiAnalysisResultRepository resultRepository,
        DiagnosticRepository diagnosticRepository,
        EvidenceFileRepository evidenceFileRepository,
        ObjectMapper objectMapper
    ) {
        this.aiRunApiClient = aiRunApiClient;
        this.resultRepository = resultRepository;
        this.diagnosticRepository = diagnosticRepository;
        this.evidenceFileRepository = evidenceFileRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Preview 호출 - 파일 추가 시 슬롯 추정
     */
    public RunPreviewResponse preview(Long diagnosticId, List<Long> fileIds) {
        Diagnostic diagnostic = getDiagnostic(diagnosticId);
        String domainCode = getDomainCode(diagnostic);

        List<FileInfo> addedFiles = fileIds.stream()
            .map(this::toFileInfo)
            .toList();

        // 기존 package_id 조회 (있으면 사용)
        String existingPackageId = resultRepository
            .findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(diagnosticId)
            .map(AiAnalysisResult::getPackageId)
            .orElse(null);

        RunPreviewRequest request = new RunPreviewRequest(
            domainCode.toLowerCase(),
            diagnostic.getPeriodStartDate().format(DATE_FORMATTER),
            diagnostic.getPeriodEndDate().format(DATE_FORMATTER),
            existingPackageId,
            addedFiles
        );

        return aiRunApiClient.previewSync(request);
    }

    /**
     * Submit 호출 - 전체 검증 및 판정 (비동기)
     */
    @Async
    @Transactional
    public CompletableFuture<AiAnalysisResult> submitAsync(Long diagnosticId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return submit(diagnosticId);
            } catch (Exception e) {
                log.error("AI 분석 실패 - diagnosticId: {}", diagnosticId, e);
                throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
            }
        });
    }

    /**
     * Submit 호출 - 전체 검증 및 판정 (동기)
     */
    @Transactional
    public AiAnalysisResult submit(Long diagnosticId) {
        Diagnostic diagnostic = getDiagnostic(diagnosticId);
        String domainCode = getDomainCode(diagnostic);

        // 증빙 파일 목록 조회
        List<EvidenceFile> evidenceFiles = evidenceFileRepository.findByDiagnosticId(diagnosticId);
        if (evidenceFiles.isEmpty()) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_MISSING_EVIDENCE);
        }

        // FileInfo 변환
        List<FileInfo> files = evidenceFiles.stream()
            .map(ef -> new FileInfo(
                ef.getResultFileId().toString(),
                ef.getFilePath(),
                ef.getOriginalFileName()
            ))
            .toList();

        // SlotHint 생성 (파일명 기반 자동 매핑)
        List<SlotHint> slotHints = evidenceFiles.stream()
            .map(ef -> new SlotHint(
                ef.getResultFileId().toString(),
                guessSlotName(ef.getOriginalFileName(), domainCode)
            ))
            .toList();

        // 기존 package_id 조회
        String packageId = resultRepository
            .findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(diagnosticId)
            .map(AiAnalysisResult::getPackageId)
            .orElse(generatePackageId(diagnostic));

        RunSubmitRequest request = new RunSubmitRequest(
            packageId,
            domainCode.toLowerCase(),
            diagnostic.getPeriodStartDate().format(DATE_FORMATTER),
            diagnostic.getPeriodEndDate().format(DATE_FORMATTER),
            files,
            slotHints
        );

        // AI API 호출
        RunSubmitResponse response = aiRunApiClient.submitSync(request);

        // 결과 저장
        return saveAnalysisResult(diagnostic, domainCode, response);
    }

    /**
     * 진단의 최신 분석 결과 조회
     */
    public AiAnalysisResult getLatestResult(Long diagnosticId) {
        return resultRepository.findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(diagnosticId)
            .orElseThrow(() -> new CustomException(ErrorCode.AI_ANALYSIS_NOT_FOUND));
    }

    /**
     * 진단의 분석 이력 조회
     */
    public List<AiAnalysisResult> getAnalysisHistory(Long diagnosticId) {
        return resultRepository.findByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(diagnosticId);
    }

    private Diagnostic getDiagnostic(Long diagnosticId) {
        return diagnosticRepository.findById(diagnosticId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));
    }

    private String getDomainCode(Diagnostic diagnostic) {
        if (diagnostic.getDomain() != null) {
            return diagnostic.getDomain().getCode();
        }
        // 레거시 데이터는 ESG로 기본 설정
        return "ESG";
    }

    private FileInfo toFileInfo(Long fileId) {
        EvidenceFile ef = evidenceFileRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        return new FileInfo(
            ef.getResultFileId().toString(),
            ef.getFilePath(),
            ef.getOriginalFileName()
        );
    }

    private String guessSlotName(String fileName, String domainCode) {
        String lowerName = fileName.toLowerCase();

        return switch (domainCode.toUpperCase()) {
            case "SAFETY" -> {
                if (lowerName.contains("tbm") || lowerName.contains("작업전")) yield "safety.tbm";
                if (lowerName.contains("교육") || lowerName.contains("education")) yield "safety.education.status";
                if (lowerName.contains("소방") || lowerName.contains("fire")) yield "safety.fire.inspection";
                if (lowerName.contains("사진") || lowerName.contains("photo")) yield "safety.site.photos";
                yield "safety.other";
            }
            case "COMPLIANCE" -> {
                if (lowerName.contains("계약") || lowerName.contains("contract")) yield "compliance.contract.sample";
                if (lowerName.contains("개인정보") || lowerName.contains("privacy")) yield "compliance.privacy.policy";
                if (lowerName.contains("교육") || lowerName.contains("education")) yield "compliance.education.status";
                yield "compliance.other";
            }
            case "ESG" -> {
                if (lowerName.contains("에너지") || lowerName.contains("energy")) yield "esg.energy.usage";
                if (lowerName.contains("고지서") || lowerName.contains("bill")) yield "esg.energy.bill";
                if (lowerName.contains("msds") || lowerName.contains("화학")) yield "esg.hazmat.msds";
                if (lowerName.contains("윤리") || lowerName.contains("ethics")) yield "esg.ethics.code";
                yield "esg.other";
            }
            default -> "other";
        };
    }

    private String generatePackageId(Diagnostic diagnostic) {
        String companyCode = diagnostic.getCompany() != null
            ? diagnostic.getCompany().getCompanyId().toString()
            : "UNKNOWN";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return String.format("PKG_%s_%s_%d", companyCode, date, diagnostic.getDiagnosticId());
    }

    @Transactional
    protected AiAnalysisResult saveAnalysisResult(
        Diagnostic diagnostic,
        String domainCode,
        RunSubmitResponse response
    ) {
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.warn("AI 응답 JSON 변환 실패", e);
            resultJson = "{}";
        }

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode(domainCode)
            .packageId(response.packageId())
            .riskLevel(response.riskLevel())
            .verdict(response.verdict())
            .whySummary(response.why())
            .resultJson(resultJson)
            .analyzedAt(LocalDateTime.now())
            .build();

        return resultRepository.save(result);
    }
}
