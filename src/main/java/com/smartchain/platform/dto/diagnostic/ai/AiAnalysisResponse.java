package com.smartchain.platform.dto.diagnostic.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    private Long diagnosticId;
    private String analysisStatus;       // PENDING, COMPLETED, FAILED
    private LocalDateTime analyzedAt;
    private int overallScore;
    private String riskLevel;            // HIGH, MEDIUM, LOW
    private CategoryScoresDto categoryScores;
    private List<HighlightDto> highlights;
    private List<RecommendationDto> recommendations;
}
