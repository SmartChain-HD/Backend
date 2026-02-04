package com.smartchain.platform.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 근거 자료 위치 정보 DTO
 */
public record SourceLocation(
    Integer page,

    @JsonProperty("line_start")
    Integer lineStart
) {
}
