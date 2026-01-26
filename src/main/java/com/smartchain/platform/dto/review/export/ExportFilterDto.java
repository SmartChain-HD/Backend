package com.smartchain.platform.dto.review.export;

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
public class ExportFilterDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> riskLevels;
    private List<Long> reviewIds;
}
