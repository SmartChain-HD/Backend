package com.smartchain.platform.dto.campaign.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatsDto {
    private int totalTargets;            // 전체 대상
    private int notStarted;              // 미시작
    private int inProgress;              // 진행중
    private int submitted;               // 제출완료
    private int approved;                // 승인완료
    private int rejected;                // 반려
}
