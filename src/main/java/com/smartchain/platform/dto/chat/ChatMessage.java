package com.smartchain.platform.dto.chat;

/**
 * 채팅 히스토리 메시지 DTO
 * AI Chat API와 통신 시 이전 대화 기록을 전달하는 데 사용
 */
public record ChatMessage(
    String role,    // "user" 또는 "assistant"
    String content
) {
}
