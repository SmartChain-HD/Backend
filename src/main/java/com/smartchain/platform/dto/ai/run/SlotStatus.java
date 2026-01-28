package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI Run API 슬롯 상태 DTO
 */
public record SlotStatus(
    @JsonProperty("slot_name")
    String slotName,

    String status  // SUBMITTED, MISSING
) {}
