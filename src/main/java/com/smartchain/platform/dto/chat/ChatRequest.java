package com.smartchain.platform.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * AI Chat API 요청 DTO
 * FE → BE: fileId로 파일 지정, BE → Python: fileUrl(presigned URL)로 변환하여 전달
 */
public record ChatRequest(
    @NotBlank(message = "메시지는 필수입니다")
    String message,

    @JsonProperty("file_id")
    Long fileId,

    @JsonProperty("file_url")
    String fileUrl,

    List<ChatMessage> history,

    String domain,

    @JsonProperty("doc_name")
    String docName,

    @Min(value = 1, message = "topK는 1 이상이어야 합니다")
    @Max(value = 30, message = "topK는 30 이하여야 합니다")
    @JsonProperty("top_k")
    Integer topK,

    @JsonProperty("session_id")
    String sessionId
) {
    public ChatRequest {
        if (history == null) {
            history = List.of();
        }
        if (domain == null || domain.isBlank()) {
            domain = "all";
        }
        if (topK == null) {
            topK = 8;
        }
    }
}
