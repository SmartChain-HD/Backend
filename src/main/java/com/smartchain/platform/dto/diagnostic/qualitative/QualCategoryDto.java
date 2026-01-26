package com.smartchain.platform.dto.diagnostic.qualitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualCategoryDto {
    private String code;                 // E, S, G
    private String name;                 // "환경", "사회", "지배구조"
    private List<QualQuestionItemDto> questions;
}
