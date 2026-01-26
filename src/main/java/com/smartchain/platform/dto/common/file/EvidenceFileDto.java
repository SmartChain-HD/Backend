package com.smartchain.platform.dto.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceFileDto {
    private Long fileId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String parsingStatus;    // PENDING, PROCESSING, SUCCESS, FAILED
    private String parsedDataUrl;    // 파싱 결과 JSON URL
    private LocalDateTime uploadedAt;
}
