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
public class DataPackageUrlResponse {
    private String downloadUrl;
    private String fileName;
    private Long fileSize;
    private LocalDateTime expiresAt;
    private PackageManifestDto manifest;
}
