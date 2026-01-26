package com.smartchain.platform.dto.diagnostic.qualitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeAssessmentResponse {
    private Long diagnosticId;
    private int totalQuestions;          // 전체 문항 수
    private int answeredQuestions;       // v3.0: answeredCount → answeredQuestions
    private int progress;                // v3.0: progressRate → progress
    private List<QualCategoryDto> categories;
}
