package com.smartchain.platform.dto.common.code;

// ==================== 에러 코드 상수 (v3.0 추가) ====================

public class ErrorCode {
    // 인증 에러
    public static final String AUTH_001 = "AUTH_001";  // 인증 토큰이 없습니다
    public static final String AUTH_002 = "AUTH_002";  // 인증 토큰이 만료되었습니다
    public static final String AUTH_003 = "AUTH_003";  // 유효하지 않은 토큰입니다
    public static final String AUTH_004 = "AUTH_004";  // 이메일 또는 비밀번호가 일치하지 않습니다
    public static final String AUTH_005 = "AUTH_005";  // 비활성화된 계정입니다
    
    // 권한 에러
    public static final String PERM_001 = "PERM_001";  // 해당 리소스에 대한 접근 권한이 없습니다
    public static final String PERM_002 = "PERM_002";  // 해당 작업을 수행할 권한이 없습니다
    public static final String PERM_003 = "PERM_003";  // 이미 처리된 권한 요청입니다
    
    // 검증 에러
    public static final String VAL_001 = "VAL_001";    // 입력값 검증에 실패했습니다
    public static final String VAL_002 = "VAL_002";    // 필수 항목이 누락되었습니다
    public static final String VAL_003 = "VAL_003";    // 파일 형식이 올바르지 않습니다
    public static final String VAL_004 = "VAL_004";    // 파일 크기가 제한을 초과했습니다
    
    // 비즈니스 에러
    public static final String BIZ_001 = "BIZ_001";    // 현재 상태에서는 해당 작업을 수행할 수 없습니다
    public static final String BIZ_002 = "BIZ_002";    // 이미 제출된 기안입니다
    public static final String BIZ_003 = "BIZ_003";    // 필수 데이터가 모두 입력되지 않았습니다
    public static final String BIZ_004 = "BIZ_004";    // 필수 증빙 파일이 누락되었습니다
    
    // 리소스 에러
    public static final String RES_001 = "RES_001";    // 요청한 리소스를 찾을 수 없습니다
    public static final String RES_002 = "RES_002";    // 사용자를 찾을 수 없습니다
    public static final String RES_003 = "RES_003";    // 진단 기안을 찾을 수 없습니다
    public static final String RES_004 = "RES_004";    // 파일을 찾을 수 없습니다
    
    // 시스템 에러
    public static final String SYS_001 = "SYS_001";    // 서버 내부 오류가 발생했습니다
    public static final String SYS_002 = "SYS_002";    // 서비스가 일시적으로 사용 불가합니다
    public static final String SYS_003 = "SYS_003";    // 파일 처리 중 오류가 발생했습니다
    
    private ErrorCode() {}
}
