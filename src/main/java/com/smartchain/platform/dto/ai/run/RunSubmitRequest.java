package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI Run API Submit 요청 DTO
 * POST /run/submit
 */
public record RunSubmitRequest(
    @JsonProperty("package_id")
    String packageId,

    String domain,  // safety, compliance, esg

    @JsonProperty("period_start")
    String periodStart,

    @JsonProperty("period_end")
    String periodEnd,

    List<FileInfo> files,

    @JsonProperty("slot_hint")
    List<SlotHint> slotHint
) {}
