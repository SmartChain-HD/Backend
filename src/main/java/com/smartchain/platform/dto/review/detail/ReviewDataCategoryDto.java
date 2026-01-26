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
public class ReviewDataCategoryDto {
    private String categoryCode;         // E, S, G
    private String categoryName;         // "환경 데이터", "사회 데이터"
    private boolean isExpanded;
    private List<ReviewDataFileDto> files;
}
