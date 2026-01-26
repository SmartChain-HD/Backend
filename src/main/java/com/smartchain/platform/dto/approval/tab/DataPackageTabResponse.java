package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageTabResponse {
    private int totalFiles;
    private String totalSizeLabel;       // "3개의 파일"
    private String zipDownloadUrl;
    private String searchKeyword;        // 검색어 (UI 상태)
    private List<DataPackageCategoryDto> categories;
}
