package com.smartchain.platform.dto.ai;

import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;

import java.time.LocalDateTime;

/**
 * AI 분석 결과 응답 DTO
 */
public record AiAnalysisResultResponse(
    Long id,
    Long diagnosticId,
    String domainCode,
    String packageId,
    String riskLevel,
    String verdict,
    String whySummary,
    Object details,
    LocalDateTime analyzedAt
) {
    public static AiAnalysisResultResponse from(AiAnalysisResult result) {
        Object details = null;
        if (result.getResultJson() != null && !result.getResultJson().isEmpty()) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                details = mapper.readValue(result.getResultJson(), Object.class);
            } catch (Exception e) {
                details = result.getResultJson();
            }
        }

        Long diagId = result.getDiagnostic() != null
            ? result.getDiagnostic().getDiagnosticId()
            : null;

        return new AiAnalysisResultResponse(
            result.getId(),
            diagId,
            result.getDomainCode(),
            result.getPackageId(),
            result.getRiskLevel(),
            result.getVerdict(),
            result.getWhySummary(),
            details,
            result.getAnalyzedAt()
        );
    }
}
