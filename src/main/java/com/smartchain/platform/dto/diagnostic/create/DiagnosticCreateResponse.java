package com.smartchain.platform.dto.diagnostic.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticCreateResponse {
    private Long diagnosticId;
    private String diagnosticCode;
    private String status;               // WRITING
    private LocalDateTime createdAt;
}
