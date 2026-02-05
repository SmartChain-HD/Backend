package com.smartchain.platform.dto.risk;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Frontend -> Backend 요청 DTO
 * POST /api/v1/risk/external/detect
 */
public record ExternalRiskDetectApiRequest(
    @NotEmpty(message = "분석 대상 회사 ID 목록은 필수입니다")
    List<Long> companyIds
) {}
