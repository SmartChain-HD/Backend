package com.smartchain.platform.dto.common.code;

/**
 * 역할 코드 상수
 * 
 * ⚠️ v3.0 변경: ADMIN 역할 제거
 */
public class RoleCode {
    public static final String GUEST = "GUEST";
    public static final String DRAFTER = "DRAFTER";
    public static final String APPROVER = "APPROVER";
    public static final String REVIEWER = "REVIEWER";
    
    // v3.0 Deprecated: ADMIN 역할 제거
    // public static final String ADMIN = "ADMIN";
    
    private RoleCode() {}
}
