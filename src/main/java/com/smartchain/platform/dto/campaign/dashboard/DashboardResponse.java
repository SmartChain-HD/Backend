package com.smartchain.platform.dto.campaign.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private UserDashboardInfoDto userInfo;
    private DashboardStatsDto stats;
    private List<DashboardTaskDto> pendingTasks;
    private List<DashboardNoticeDto> notices;
}
