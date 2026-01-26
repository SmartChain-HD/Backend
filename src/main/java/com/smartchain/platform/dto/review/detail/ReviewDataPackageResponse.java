package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDataPackageResponse {
    private String zipDownloadUrl;
    private int totalFiles;              // "총 3개의 파일"
    private String totalSizeLabel;
    private String searchKeyword;
    private List<ReviewDataCategoryDto> categories;
}
