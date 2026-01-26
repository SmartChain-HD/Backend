package com.smartchain.platform.dto.review.decision;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDecisionRequest {
    
    @NotBlank(message = "심사 결과를 선택해주세요")
    private String decision;             // APPROVED, REVISION_REQUIRED
    
    private String comment;              // 코멘트
    
    private Map<String, String> categoryComments;  // v3.0: 카테고리별 코멘트 추가
    
    private List<RevisionRequestItemDto> revisionItems; // 보완 요청 항목 (보완요청시)
}
