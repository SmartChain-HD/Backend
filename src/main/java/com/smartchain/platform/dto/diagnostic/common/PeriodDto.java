package com.smartchain.platform.dto.diagnostic.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDto {
    private LocalDate startDate;
    private LocalDate endDate;
}
