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
public class QualAssessmentSaveRequest {
    private List<QualAnswerDto> answers;
}
