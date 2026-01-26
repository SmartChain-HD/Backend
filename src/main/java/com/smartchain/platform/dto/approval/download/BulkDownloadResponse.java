package com.smartchain.platform.dto.approval.download;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDownloadResponse {
    private String downloadUrl;          // ZIP 파일 URL
    private int totalFiles;
    private String totalSizeLabel;
    private LocalDateTime expiresAt;     // 다운로드 링크 만료 시간
}
