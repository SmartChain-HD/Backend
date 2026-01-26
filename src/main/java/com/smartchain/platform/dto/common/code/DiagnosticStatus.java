package com.smartchain.platform.dto.common.code;

/**
 * 진단 상태 코드
 */
public class DiagnosticStatus {
    public static final String WRITING = "WRITING";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String RETURNED = "RETURNED";
    public static final String APPROVED = "APPROVED";
    public static final String REVIEWING = "REVIEWING";
    public static final String COMPLETED = "COMPLETED";
    
    private DiagnosticStatus() {}
}
