package com.smartchain.platform.dto.chat;

/**
 * 채팅 응답의 근거 자료 DTO
 */
public record SourceItem(
    String title,           // 문서 제목 또는 파일명
    String type,            // 자료 유형: "manual", "code", "law"
    String snippet,         // 실제 참고한 본문 내용 (일부)
    Double score,           // 검색 유사도 점수 (0.0 ~ 1.0)
    SourceLocation loc      // 위치 정보 (page, lineStart)
) {
}
