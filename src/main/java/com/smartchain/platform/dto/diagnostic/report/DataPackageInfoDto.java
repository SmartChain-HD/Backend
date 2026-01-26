package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageInfoDto {
    private String downloadUrl;          // ZIP 다운로드 URL
    private int totalFiles;
    private String totalSizeLabel;       // "15.3 MB"
    private List<DataPackageFileDto> files;
}
