package com.smartchain.platform.dto.review.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListRequest {
    private LocalDate fromDate;          // 기간 필터 시작
    private LocalDate toDate;            // 기간 필터 종료
    private String companyName;          // 협력사명 검색
    private Long companyId;              // 협력사 ID 필터
    private List<String> riskLevels;     // 위험군 필터 (HIGH, MEDIUM, LOW)
    private List<String> statuses;       // 상태 필터
    private Integer page;
    private Integer size;
}
