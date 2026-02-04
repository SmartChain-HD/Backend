package com.smartchain.platform.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI Chat Admin DB 현황 조회 API 응답 DTO
 */
public record AdminInspectResponse(
    @JsonProperty("total_documents")
    Integer totalDocuments,     // Vector DB에 저장된 문서 개수

    List<String> samples        // 샘플 데이터 목록
) {
}
