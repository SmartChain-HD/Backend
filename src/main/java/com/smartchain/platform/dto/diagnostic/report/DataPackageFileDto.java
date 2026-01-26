package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageFileDto {
    private String fileName;
    private String category;             // "환경 데이터", "사회 데이터"
    private String fileSizeLabel;        // "2.4 MB"
    private LocalDateTime uploadedAt;
    private String uploadedAtLabel;      // "2026-01-06"
}
