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
public class ReportTocSubItemDto {
    private String subSectionCode;       // "E", "S", "G"
    private String subSectionTitle;      // "E (Environment)"
    private int pageNumber;
    private List<ReportTocIndicatorDto> indicators;
}
