package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificationLogItemDto {
    private Long logId;
    private LocalDate date;
    private String dateLabel;            // "2026-01-07"
    private String modifierName;         // "김수정"
    private String field;                // "항목"
    private String action;               // "값 수정"
    private String status;               // "승인"
    private String previousValue;        // "1,000"
    private String newValue;             // "500"
    private boolean hasReason;
    private String reason;               // "지표가 다르게 산출되었습니다"
}
