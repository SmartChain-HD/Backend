package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantitativeUploadPageResponse {
    private Long diagnosticId;
    private List<StandardOptionDto> availableStandards;      // 공시 기준 선택지
    private List<String> selectedStandards;                  // 선택된 공시 기준
    private DataUploadStatusDto uploadStatus;                // 업로드 현황
    private List<DataSlotCategoryDto> slotCategories;        // E, S, G별 슬롯
    private int progressRate;
}
