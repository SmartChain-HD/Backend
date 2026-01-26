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
public class ReportTocItemDto {
    private String sectionCode;          // "I", "II"
    private String sectionTitle;         // "정성적 평가", "정량적 평가"
    private int pageNumber;
    private List<ReportTocSubItemDto> subItems;
}
