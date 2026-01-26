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
public class FileUploadResponse {
    private Long fileId;
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private String fileType;         // PDF, XLSX, DOCX, PNG, JPG
    private String mimeType;         // v3.0: MIME 타입 추가
    private Long fileSize;           // bytes
    private String fileSizeLabel;    // v3.0: 사람이 읽기 쉬운 형식 추가
    private String uploadStatus;     // UPLOADING, SUCCESS, FAILED
    private String errorMessage;     // 실패시 원인
    private LocalDateTime uploadedAt;
    
    // v3.0: 비동기 처리 관련
    private String jobId;            // 파싱 작업 ID
    private String statusCheckUrl;   // 상태 확인 URL
}
