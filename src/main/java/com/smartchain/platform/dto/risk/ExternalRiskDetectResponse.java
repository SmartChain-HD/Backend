package com.smartchain.platform.dto.risk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI 서버 -> Backend 응답 DTO
 */
public record ExternalRiskDetectResponse(
    List<VendorRiskResult> results
) {
    public record VendorRiskResult(
        String vendor,
        @JsonProperty("external_risk_level") String riskLevel,
        @JsonProperty("total_score") Double totalScore,
        @JsonProperty("docs_count") Integer docsCount,
        @JsonProperty("reason_1line") String summary,
        @JsonProperty("reason_3lines") List<String> reason3lines,
        List<Evidence> evidence
    ) {}

    public record Evidence(
        @JsonProperty("doc_id") String docId,
        String source,
        String title,
        String snippet,
        String url,
        @JsonProperty("published_at") String date
    ) {}
}
