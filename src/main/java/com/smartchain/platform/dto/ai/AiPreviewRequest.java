package com.smartchain.platform.dto.ai;

import java.util.List;

/**
 * AI Preview 요청 DTO
 */
public record AiPreviewRequest(
    List<Long> fileIds
) {}
