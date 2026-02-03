package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * AI Run API Submit 응답 DTO
 * 고정 Output 스키마
 */
public record RunSubmitResponse(
    @JsonProperty("package_id")
    String packageId,

    @JsonProperty("risk_level")
    String riskLevel,  // LOW, MEDIUM, HIGH

    String verdict,  // PASS, WARN, NEED_CLARIFY, NEED_FIX

    String why,  // 한 줄 요약

    @JsonProperty("slot_results")
    List<SlotResult> slotResults,

    List<Clarification> clarifications,

    Map<String, Object> extras  // 도메인별 추가 정보
) {}
