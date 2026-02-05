package com.smartchain.platform.dto.risk;

import com.smartchain.platform.domain.risk.entity.ExternalRiskResult;

import java.time.LocalDateTime;

/**
 * Backend -> Frontend 응답 DTO
 */
public record ExternalRiskResultResponse(
    Long id,
    Long companyId,
    String companyName,
    String riskLevel,
    String summary,
    String evidenceJson,
    LocalDateTime detectedAt
) {
    public static ExternalRiskResultResponse from(ExternalRiskResult entity) {
        return new ExternalRiskResultResponse(
            entity.getId(),
            entity.getCompanyId(),
            entity.getCompanyName(),
            entity.getRiskLevel().name(),
            entity.getSummary(),
            entity.getEvidenceJson(),
            entity.getDetectedAt()
        );
    }
}
