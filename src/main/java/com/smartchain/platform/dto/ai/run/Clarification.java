package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI Run API 보완요청 메시지 DTO
 */
public record Clarification(
    @JsonProperty("slot_name")
    String slotName,

    String message,

    @JsonProperty("file_ids")
    List<String> fileIds
) {}
