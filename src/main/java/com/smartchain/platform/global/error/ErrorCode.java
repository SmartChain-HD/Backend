package com.smartchain.platform.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "U001", "입력값이 올바르지 않습니다"),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U004", "비밀번호가 일치하지 않습니다"),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "U005", "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다"),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "U006", "필수 약관에 동의해야 합니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "U007", "이메일 인증이 필요합니다"),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "U008", "인증 코드가 만료되었습니다"),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "U009", "인증 코드가 올바르지 않습니다"),
    VERIFICATION_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "U010", "잠시 후 다시 시도해주세요"),

    // 401 Unauthorized
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "이메일 또는 비밀번호가 올바르지 않습니다"),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다"),
    PERMISSION_DENIED_RESOURCE(HttpStatus.FORBIDDEN, "PERM_001", "해당 리소스에 대한 접근 권한이 없습니다"),
    PERMISSION_DENIED_ACTION(HttpStatus.FORBIDDEN, "PERM_002", "해당 작업을 수행할 권한이 없습니다"),

    // 400 Bad Request - File Validation
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "VAL_003", "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "VAL_004", "파일 크기가 제한을 초과했습니다"),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U003", "사용자를 찾을 수 없습니다"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "역할을 찾을 수 없습니다"),
    DOMAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "DOM001", "도메인을 찾을 수 없습니다"),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "회사를 찾을 수 없습니다"),
    DIAGNOSTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "진단을 찾을 수 없습니다"),
    CAMPAIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "캠페인을 찾을 수 없습니다"),
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "AP001", "결재를 찾을 수 없습니다"),
    EVIDENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "증빙파일을 찾을 수 없습니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_004", "파일을 찾을 수 없습니다"),
    ROLE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "R002", "권한 요청을 찾을 수 없습니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "RV001", "심사를 찾을 수 없습니다"),
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB001", "작업을 찾을 수 없습니다"),

    // 422 Unprocessable Entity (Business Logic Errors)
    DIAGNOSTIC_CANNOT_SUBMIT(HttpStatus.UNPROCESSABLE_ENTITY, "D003", "제출할 수 없는 상태입니다"),
    DIAGNOSTIC_INCOMPLETE_DATA(HttpStatus.UNPROCESSABLE_ENTITY, "BIZ_003", "필수 데이터가 모두 입력되지 않았습니다"),
    DIAGNOSTIC_MISSING_EVIDENCE(HttpStatus.UNPROCESSABLE_ENTITY, "BIZ_004", "필수 증빙 파일이 누락되었습니다"),

    // 409 Conflict - Business Logic
    DIAGNOSTIC_INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "BIZ_001", "현재 상태에서는 해당 작업을 수행할 수 없습니다"),
    DIAGNOSTIC_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "BIZ_002", "이미 제출된 기안입니다"),

    // 409 Conflict
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "C002", "이미 등록된 사업자등록번호입니다"),
    DUPLICATE_ROLE_REQUEST(HttpStatus.CONFLICT, "R003", "이미 대기중인 권한 요청이 있습니다"),
    DUPLICATE_DOMAIN_ROLE(HttpStatus.CONFLICT, "R006", "해당 도메인에 이미 권한이 부여되어 있습니다"),
    INVALID_ROLE_REQUEST(HttpStatus.BAD_REQUEST, "R004", "유효하지 않은 권한입니다"),
    ALREADY_PROCESSED_REQUEST(HttpStatus.CONFLICT, "PERM_003", "이미 처리된 권한 요청입니다"),
    INVALID_DECISION(HttpStatus.BAD_REQUEST, "R005", "유효하지 않은 처리 결과입니다"),
    ALREADY_PROCESSED_APPROVAL(HttpStatus.CONFLICT, "AP002", "이미 처리된 결재 요청입니다"),
    APPROVAL_NOT_APPROVED(HttpStatus.CONFLICT, "AP003", "승인된 결재만 원청에 제출할 수 있습니다"),
    DIAGNOSTIC_NOT_APPROVED(HttpStatus.CONFLICT, "AP004", "승인된 진단만 원청에 제출할 수 있습니다"),
    ALREADY_PROCESSED_REVIEW(HttpStatus.CONFLICT, "RV002", "이미 처리된 심사입니다"),
    INVALID_REVIEW_DECISION(HttpStatus.BAD_REQUEST, "RV003", "유효하지 않은 심사 결과입니다"),
    JOB_NOT_RETRYABLE(HttpStatus.BAD_REQUEST, "JOB002", "재시도할 수 없는 작업입니다"),
    JOB_NOT_FAILED(HttpStatus.BAD_REQUEST, "JOB003", "실패한 작업만 재시도할 수 있습니다"),

    // 500 Internal Server Error
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 오류가 발생했습니다"),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "파일 업로드에 실패했습니다"),
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "AI 서비스 연동에 실패했습니다"),
    FILE_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_003", "파일 파싱에 실패했습니다"),

    // 503 Service Unavailable - AI Service
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI001", "AI 서비스에 연결할 수 없습니다"),
    AI_ANALYSIS_IN_PROGRESS(HttpStatus.CONFLICT, "AI002", "이미 분석이 진행 중입니다"),
    AI_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "AI003", "AI 분석 결과를 찾을 수 없습니다"),
    AI_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "AI004", "AI 서비스 응답이 유효하지 않습니다"),
    AI_INVALID_VERDICT(HttpStatus.BAD_GATEWAY, "AI005", "AI 서비스의 verdict 값이 유효하지 않습니다"),
    AI_INVALID_RISK_LEVEL(HttpStatus.BAD_GATEWAY, "AI006", "AI 서비스의 riskLevel 값이 유효하지 않습니다"),
    AI_BAD_REQUEST(HttpStatus.BAD_REQUEST, "AI007", "AI 서비스 요청 파라미터가 올바르지 않습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}