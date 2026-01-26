package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogExportRequest {
    private String format;               // CSV, XLSX
    private ActivityLogFilterDto filters;
}
