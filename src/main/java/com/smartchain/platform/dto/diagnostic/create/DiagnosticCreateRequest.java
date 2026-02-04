package com.smartchain.platform.dto.diagnostic.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticCreateRequest {
    @NotBlank(message = "기안 제목을 입력해주세요")
    private String title;

    @NotNull(message = "캠페인을 선택해주세요")
    private Long campaignId;

    @NotNull(message = "도메인을 선택해주세요")
    private String domainCode;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;
}
