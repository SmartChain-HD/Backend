package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogFilterDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long userId;
    private String actionType;
}
