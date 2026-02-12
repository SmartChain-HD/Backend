package com.smartchain.platform.dto.ai;

import java.util.List;

/**
 * AI Preview 요청 DTO
 */
public record AiPreviewRequest(
    List<Long> fileIds,
    List<String> removedFileIds  // AI 패키지에서 제거할 파일 ID 목록 (optional)
) {}
