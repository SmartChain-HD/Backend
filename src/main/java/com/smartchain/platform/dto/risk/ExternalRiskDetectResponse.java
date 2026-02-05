package com.smartchain.platform.dto.risk;

import java.util.List;

/**
 * AI 서버 -> Backend 응답 DTO
 */
public record ExternalRiskDetectResponse(
    List<VendorRiskResult> results
) {
    public record VendorRiskResult(
        String vendor,
        String riskLevel,
        String summary,
        List<Evidence> evidence
    ) {}

    public record Evidence(
        String source,
        String title,
        String snippet,
        String url,
        String date
    ) {}
}
