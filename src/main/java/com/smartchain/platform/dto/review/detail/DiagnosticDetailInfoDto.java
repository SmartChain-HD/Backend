package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticDetailInfoDto {
    private Long diagnosticId;
    private String diagnosticCode;
    private Object qualitativeData;      // 정성적 데이터
    private Object quantitativeData;     // 정량적 데이터
    private Object aiAnalysis;           // AI 분석 결과
}
