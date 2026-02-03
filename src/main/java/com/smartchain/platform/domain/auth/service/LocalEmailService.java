package com.smartchain.platform.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 로컬 개발용 이메일 서비스.
 * 실제 발송 없이 로그로 인증 코드를 출력합니다.
 * local 및 test 프로파일에서 활성화됩니다.
 */
@Slf4j
@Service
@Profile({"local", "test"})
public class LocalEmailService implements EmailService {

    @Override
    public void sendVerificationCode(String to, String code, int expiryMinutes) {
        log.info("========================================");
        log.info("[LOCAL] Email verification code");
        log.info("To: {}", to);
        log.info("Code: {}", code);
        log.info("Expires in: {} minutes", expiryMinutes);
        log.info("========================================");
    }
}
