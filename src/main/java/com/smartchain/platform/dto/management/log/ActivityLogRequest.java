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
public class ActivityLogRequest {
    private Long userId;                 // 특정 사용자 (null이면 전체)
    private LocalDate fromDate;
    private LocalDate toDate;
    private String actionType;           // LOGIN, UPLOAD, SUBMIT, APPROVE 등
    private Integer page;
    private Integer size;
}
