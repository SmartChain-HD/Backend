package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileInfoDto {
    private Long fileId;
    private String fileName;
    private String fileType;             // PDF, XLSX, DOCX, PNG
    private String fileTypeIcon;         // 파일 타입 아이콘 URL
    private Long fileSize;
    private String fileSizeLabel;        // "2.4 MB"
    private LocalDateTime uploadedAt;
}
