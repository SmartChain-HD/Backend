package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI Run API 슬롯 힌트 DTO
 * 파일과 슬롯 매핑 정보
 */
public record SlotHint(
    @JsonProperty("file_id")
    String fileId,

    @JsonProperty("slot_name")
    String slotName,

    Double confidence,

    @JsonProperty("match_reason")
    String matchReason
) {
    public SlotHint(String fileId, String slotName) {
        this(fileId, slotName, null, null);
    }
}
