package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewActionDto {
    private String actionCode;           // CHANGE_RISK_LEVEL, SET_RESULT, DOWNLOAD_ZIP
    private String actionLabel;          // "ESG 위험군 수기 변경", "심사 결과 설정"
    private String actionType;           // PRIMARY, SECONDARY
    private boolean enabled;
}
