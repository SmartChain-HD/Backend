package com.smartchain.platform.dto.approval.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatsDto {
    private int waiting;                 // 대기
    private int approved;                // 승인
    private int rejected;                // 반려
}
