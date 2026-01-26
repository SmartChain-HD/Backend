package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDataFileDto {
    private Long fileId;
    private String fileName;
    private String fileSizeLabel;
    private LocalDateTime uploadedAt;
    private String uploadedAtLabel;
    private String downloadUrl;
    private String previewUrl;           // 미리보기 URL (지원시)
}
