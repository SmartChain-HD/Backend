package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI Run API 슬롯 상태 DTO
 */
public record SlotStatus(
    @JsonProperty("slot_name")
    String slotName,

    @JsonProperty("display_name")
    String displayName,

    String status  // SUBMITTED, MISSING
) {
    public SlotStatus(String slotName, String status) {
        this(slotName, "", status);
    }
}
