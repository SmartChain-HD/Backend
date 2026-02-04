package com.smartchain.platform.dto.ai;

import com.smartchain.platform.dto.ai.run.SlotHint;

import java.util.List;
import java.util.Map;

/**
 * AI 분석 요청 DTO
 */
public record AiAnalysisRequest(
    List<Long> fileIds,
    List<SlotHint> slotHints,
    Map<String, Object> options
) {}
