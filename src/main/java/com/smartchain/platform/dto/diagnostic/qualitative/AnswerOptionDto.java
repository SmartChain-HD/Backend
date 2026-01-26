package com.smartchain.platform.dto.diagnostic.qualitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionDto {
    private String value;                // "Y", "N", "NA"
    private String label;                // "예", "아니오", "해당없음"
    private Integer score;               // 100, 0, null
}
