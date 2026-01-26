package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModificationLogResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<ColumnOptionDto> columnOptions;     // 표시 항목 선택지
    private List<String> selectedColumns;
    private List<ReviewLogItemDto> logs;
    private String downloadUrl;          // 로그 다운로드 URL
}
