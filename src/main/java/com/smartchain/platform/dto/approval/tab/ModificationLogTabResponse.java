package com.smartchain.platform.dto.approval.tab;

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
public class ModificationLogTabResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> availableColumns;   // 선택 가능한 컬럼
    private List<String> selectedColumns;    // 선택된 컬럼
    private List<ModificationLogItemDto> logs;
}
