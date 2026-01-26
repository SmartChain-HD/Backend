package com.smartchain.platform.dto.diagnostic.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticHistoryItemDto {
    private Long historyId;
    private String action;               // CREATED, SUBMITTED, APPROVED, REJECTED, etc.
    private String previousStatus;
    private String newStatus;
    private PerformedByDto performedBy;
    private String comment;
    private LocalDateTime timestamp;
}
