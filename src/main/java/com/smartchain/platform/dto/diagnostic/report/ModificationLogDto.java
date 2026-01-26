package com.smartchain.platform.dto.diagnostic.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificationLogDto {
    private LocalDate date;
    private String modifierName;
    private String field;                // "항목"
    private String action;               // "값 수정"
    private String status;               // "승인"
    private String previousValue;
    private String newValue;
    private String reason;
    private boolean hasReason;
}
