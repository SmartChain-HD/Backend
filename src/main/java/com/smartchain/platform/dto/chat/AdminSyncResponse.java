package com.smartchain.platform.dto.chat;

/**
 * AI Chat Admin 동기화 API 응답 DTO
 */
public record AdminSyncResponse(
    String status,      // "accepted" 등
    String message      // 상태 메시지
) {
}
