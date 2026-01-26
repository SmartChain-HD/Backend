package com.smartchain.platform.dto.diagnostic.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticSubmitErrorResponse {
    private int qualitativeProgress;
    private int quantitativeProgress;
    private List<String> missingRequired;  // 누락된 필수 항목 코드
}
