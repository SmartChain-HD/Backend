package com.smartchain.platform.dto.management.log;

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
public class ActivityLogResponse {
    private List<ActivityLogItemDto> content;  // v3.0: logs → content
    private PageDto page;
}
