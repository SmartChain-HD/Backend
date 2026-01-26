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
public class ModificationLogTabRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> displayColumns; // 표시할 컬럼 선택
}
