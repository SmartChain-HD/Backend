package com.smartchain.platform.dto.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartchain.platform.domain.user.entity.Company;

import java.util.List;

/**
 * Backend -> AI 서버 요청 DTO
 * POST /risk/external/detect
 *
 * AI 서버 스키마: vendors는 회사명 문자열 배열
 */
public record ExternalRiskDetectRequest(
    List<String> vendors,
    RagConfig rag,
    SearchConfig search
) {
    public record RagConfig(boolean enabled) {}
    public record SearchConfig(
        @JsonProperty("time_window_days") int timeWindowDays,
        @JsonProperty("max_results") int maxResults
    ) {}

    public static ExternalRiskDetectRequest of(List<Company> companies) {
        List<String> vendorNames = companies.stream()
            .map(Company::getName)
            .toList();
        return new ExternalRiskDetectRequest(
            vendorNames,
            new RagConfig(false),
            new SearchConfig(365, 30)
        );
    }
}
