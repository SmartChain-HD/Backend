package com.smartchain.platform.dto.approval.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticSimpleDto {
    private Long diagnosticId;
    private String diagnosticCode;
    private String title;
}
