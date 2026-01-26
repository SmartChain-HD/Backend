package com.smartchain.platform.dto.management.user;

import com.smartchain.platform.dto.management.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private UserManagementStatsDto stats;
    private List<UserManagementItemDto> content;  // v3.0: users → content
    private PageDto page;
}
