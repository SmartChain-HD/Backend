package com.smartchain.platform.dto.review.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    private String format;               // CSV, XLSX
    private ExportFilterDto filters;     // v3.0: 구조화
    private List<String> columns;        // v3.0: includeFields → columns
}
