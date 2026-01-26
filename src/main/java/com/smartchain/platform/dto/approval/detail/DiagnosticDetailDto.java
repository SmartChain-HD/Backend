package com.smartchain.platform.dto.approval.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticDetailDto {
    private Long diagnosticId;
    private String diagnosticCode;
    private String title;
    private int qualitativeProgress;
    private int quantitativeProgress;
    private Integer overallScore;
}
