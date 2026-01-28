package com.smartchain.platform.dto.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 분석 요청 DTO
 */
public record AiAnalysisRequest(
    List<Long> fileIds,
    Map<String, Object> options
) {}
