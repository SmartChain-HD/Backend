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
public class DiagnosticSubmitStatusDto {
    private boolean canSubmit;           // 제출 가능 여부
    private List<String> pendingItems;   // 미완료 항목
    private String message;
}
