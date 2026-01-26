package com.smartchain.platform.dto.diagnostic.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticCreateRequest {
    @NotNull(message = "캠페인을 선택해주세요")
    private Long campaignId;
}
