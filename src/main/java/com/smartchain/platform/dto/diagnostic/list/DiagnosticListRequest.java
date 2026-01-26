package com.smartchain.platform.dto.diagnostic.list;

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
public class DiagnosticListRequest {
    private LocalDate deadlineFrom;      // 마감일 필터 시작
    private LocalDate deadlineTo;        // 마감일 필터 종료
    private List<String> statuses;       // 상태 필터 (복수 선택)
    private Integer page;
    private Integer size;
    private String sort;                 // v3.0: 정렬 옵션 추가 (createdAt,desc)
}
