package com.smartchain.platform.dto.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileParsingResultResponse {
    private Long fileId;
    private String fileName;
    private String parsingStatus;      // WAITING, PROCESSING, SUCCESS, FAILED
    private BigDecimal parsedValue;     // 파싱된 수치 값
    private String parsedText;          // 파싱된 텍스트 값
    private String metaInfo;            // 파싱 메타 정보 (JSON)
    private String errorMessage;        // 실패 시 오류 메시지
    private LocalDateTime completedAt;
}
