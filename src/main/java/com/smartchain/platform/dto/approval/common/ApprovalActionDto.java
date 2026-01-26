package com.smartchain.platform.dto.approval.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionDto {
    private String actionCode;           // APPROVE, REJECT, DOWNLOAD, SUBMIT_TO_REVIEWER
    private String actionLabel;          // "결재 결과 설정", "원청 제출"
    private String actionType;           // PRIMARY, SECONDARY, DANGER
    private boolean enabled;
}
