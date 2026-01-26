package com.smartchain.platform.dto.diagnostic.qualitative;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualAnswerDto {
    @NotNull
    private Long questionId;

    private String value;                // v3.0: answer(Boolean) → value(String)
}
