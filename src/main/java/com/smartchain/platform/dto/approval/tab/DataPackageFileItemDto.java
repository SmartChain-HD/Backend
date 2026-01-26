package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageFileItemDto {
    private Long fileId;
    private String fileName;             // "온실가스_배출량.xlsx"
    private String fileSizeLabel;        // "2.4 MB"
    private LocalDateTime uploadedAt;
    private String uploadedAtLabel;      // "2026-01-06"
    private String downloadUrl;
}
