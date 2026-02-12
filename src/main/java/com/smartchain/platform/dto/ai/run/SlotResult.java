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

    @JsonProperty("display_name")
    String displayName,

    String verdict,  // PASS, NEED_CLARIFY, NEED_FIX

    List<String> reasons,

    @JsonProperty("file_ids")
    List<String> fileIds,

    @JsonProperty("file_names")
    List<String> fileNames,

    Map<String, Object> extras
) {
    /**
     * displayName 없이 생성하는 생성자 (AI 서버 응답 역직렬화용)
     */
    public SlotResult(String slotName, String verdict, List<String> reasons,
                      List<String> fileIds, List<String> fileNames, Map<String, Object> extras) {
        this(slotName, null, verdict, reasons, fileIds, fileNames, extras);
    }

    /**
     * displayName을 추가한 새 SlotResult 반환
     */
    public SlotResult withDisplayName(String displayName) {
        return new SlotResult(slotName, displayName, verdict, reasons, fileIds, fileNames, extras);
    }
}
