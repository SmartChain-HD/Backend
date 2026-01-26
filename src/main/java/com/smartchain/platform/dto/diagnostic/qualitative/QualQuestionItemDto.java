package com.smartchain.platform.dto.diagnostic.qualitative;

import com.smartchain.platform.dto.diagnostic.common.EvidenceFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualQuestionItemDto {
    private Long questionId;
    private String questionCode;         // "E-001"
    private String questionText;         // v3.0: content → questionText
    private String category;
    private String subCategory;          // v3.0: area → subCategory
    private boolean required;
    private String answerType;           // SINGLE_CHOICE
    private List<AnswerOptionDto> options;
    private CurrentAnswerDto currentAnswer;
    private boolean evidenceRequired;
    private List<EvidenceFileDto> evidenceFiles;
}
