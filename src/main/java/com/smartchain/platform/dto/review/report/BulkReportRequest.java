package com.smartchain.platform.dto.review.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkReportRequest {
    private List<Long> reviewIds;        // v3.0: auditIds → reviewIds
    private String reportType;           // FULL, SUMMARY
}
