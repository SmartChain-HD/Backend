package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * AI Run API 슬롯 검증 결과 DTO
 */
public record SlotResult(
    @JsonProperty("slot_name")
    String slotName,

    String verdict,  // PASS, NEED_CLARIFY, NEED_FIX

    List<String> reasons,

    @JsonProperty("file_ids")
    List<String> fileIds,

    @JsonProperty("file_names")
    List<String> fileNames,

    Map<String, String> extras
) {}
