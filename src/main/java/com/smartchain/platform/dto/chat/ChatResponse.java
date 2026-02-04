package com.smartchain.platform.dto.chat;

import java.util.List;

/**
 * AI Chat API 응답 DTO
 */
public record ChatResponse(
    String answer,              // AI가 생성한 최종 답변
    String confidence,          // 답변 신뢰도: "high", "medium", "low"
    String notes,               // 비고 (예: "근거 자료 없음")
    List<SourceItem> sources    // 답변에 사용된 근거 자료 목록
) {
}
