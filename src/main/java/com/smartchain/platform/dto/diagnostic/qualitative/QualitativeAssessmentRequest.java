package com.smartchain.platform.dto.diagnostic.qualitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeAssessmentRequest {
    private String category;             // E, S, G (필터)
}
