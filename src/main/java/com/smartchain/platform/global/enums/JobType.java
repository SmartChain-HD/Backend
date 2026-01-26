package com.smartchain.platform.global.enums;

/**
 * 비동기 작업 유형
 */
public enum JobType {
    FILE_PARSING,       // 파일 파싱
    REPORT_GENERATION,  // 단일 보고서 생성
    BULK_REPORT,        // 일괄 보고서 생성
    EXPORT              // CSV/Excel 내보내기
}
