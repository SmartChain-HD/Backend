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
public class DataPackageCategoryDto {
    private String categoryName;         // "환경 데이터", "사회 데이터"
    private boolean isExpanded;          // 펼침 여부
    private List<DataPackageFileItemDto> files;
}
