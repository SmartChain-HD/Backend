package com.smartchain.platform.dto.common.code;

/**
 * 권한 요청 상태 코드
 */
public class AccessRequestStatus {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    
    private AccessRequestStatus() {}
}
