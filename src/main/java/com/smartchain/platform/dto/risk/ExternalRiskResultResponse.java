package com.smartchain.platform.dto.risk;

import com.smartchain.platform.domain.risk.entity.ExternalRiskResult;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    OffsetDateTime detectedAt
) {
    private static final ZoneOffset KST_OFFSET = ZoneOffset.ofHours(9);

    public static ExternalRiskResultResponse from(ExternalRiskResult entity) {
        LocalDateTime detectedAt = entity.getDetectedAt();
        return new ExternalRiskResultResponse(
            entity.getId(),
            entity.getCompanyId(),
            entity.getCompanyName(),
            entity.getRiskLevel().name(),
            entity.getSummary(),
            entity.getEvidenceJson(),
            detectedAt != null ? detectedAt.atOffset(KST_OFFSET) : null
        );
    }
}
