package com.smartchain.platform.dto.approval.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitToReviewerRequest {
    private String submitComment;        // 제출 코멘트
}
