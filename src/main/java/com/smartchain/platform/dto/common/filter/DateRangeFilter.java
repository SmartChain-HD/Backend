package com.smartchain.platform.dto.common.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeFilter {
    private String fromDate;     // yyyy-MM-dd
    private String toDate;       // yyyy-MM-dd
}
