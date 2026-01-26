package com.smartchain.platform.dto.diagnostic.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticHistoryResponse {
    private Long diagnosticId;
    private List<DiagnosticHistoryItemDto> history;
}
