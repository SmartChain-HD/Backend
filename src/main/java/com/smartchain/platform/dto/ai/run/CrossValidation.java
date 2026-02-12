package com.smartchain.platform.dto.ai.run;

import java.util.List;
import java.util.Map;

/**
 * AI Run API 교차 검증 결과 DTO
 * __x__ 구분자로 연결된 슬롯 간 교차 검증 결과를 표현
 */
public record CrossValidation(
    List<String> slots,           // 비교 대상 슬롯명 (예: ["safety.education.attendance", "safety.education.photo"])
    List<String> displayNames,    // 비교 대상 한글 표시명 (예: ["교육 출석부", "교육일 사진"])
    String verdict,               // PASS, NEED_FIX 등
    List<String> reasons,
    Map<String, Object> extras    // 교차 검증 추가 데이터 (예: {"attendance_count": "9", "photo_count": "10"})
) {}
