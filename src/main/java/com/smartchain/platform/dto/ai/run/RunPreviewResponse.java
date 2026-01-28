package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI Run API Preview 응답 DTO
 */
public record RunPreviewResponse(
    @JsonProperty("package_id")
    String packageId,

    @JsonProperty("slot_hint")
    List<SlotHint> slotHint,

    @JsonProperty("required_slot_status")
    List<SlotStatus> requiredSlotStatus,

    @JsonProperty("missing_required_slots")
    List<String> missingRequiredSlots
) {}
