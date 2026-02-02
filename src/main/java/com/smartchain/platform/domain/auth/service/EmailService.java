package com.smartchain.platform.domain.auth.service;

/**
 * 이메일 발송 서비스 인터페이스.
 * 프로파일에 따라 실제 SMTP 발송 또는 로깅으로 구현됩니다.
 */
public interface EmailService {

    /**
     * 이메일 인증 코드를 발송합니다.
     *
     * @param to 수신자 이메일 주소
     * @param code 인증 코드 (6자리 숫자)
     * @param expiryMinutes 인증 코드 만료 시간 (분)
     */
    void sendVerificationCode(String to, String code, int expiryMinutes);
}
