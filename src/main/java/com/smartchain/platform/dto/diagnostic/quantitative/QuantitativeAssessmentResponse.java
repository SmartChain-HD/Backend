package com.smartchain.platform.dto.diagnostic.quantitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantitativeAssessmentResponse {
    private Long diagnosticId;
    private int totalIndicators;
    private int filledIndicators;
    private int progress;
    private List<QuantCategoryDto> categories;
}
